package br.com.maxsueleinstein.cuponomia.infrastructure.persistence.adapter;

import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.model.CouponCode;
import br.com.maxsueleinstein.cuponomia.domain.model.DiscountType;
import br.com.maxsueleinstein.cuponomia.domain.repository.CouponRepository;
import br.com.maxsueleinstein.cuponomia.domain.rule.CouponRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.ExpirationDateRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.MaxUsageRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.MinimumOrderValueRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.SingleUsePerClientRule;
import br.com.maxsueleinstein.cuponomia.infrastructure.persistence.entity.CouponEntity;
import br.com.maxsueleinstein.cuponomia.infrastructure.persistence.jpa.JpaCouponRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the domain CouponRepository port using Spring Data JPA.
 * <p>
 * Handles conversion between domain Coupon and JPA CouponEntity.
 * This is the only place where JPA-specific code touches domain objects.
 */
@Component
public class CouponRepositoryAdapter implements CouponRepository {

    private final JpaCouponRepository jpaRepository;

    public CouponRepositoryAdapter(JpaCouponRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Coupon save(Coupon coupon) {
        CouponEntity entity;
        // Check if this is an update (entity already exists in DB)
        Optional<CouponEntity> existing = jpaRepository.findById(coupon.getId());
        if (existing.isPresent()) {
            entity = existing.get();
            updateEntity(entity, coupon);
        } else {
            entity = toEntity(coupon);
        }
        CouponEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Coupon> findByCode(String code) {
        return jpaRepository.findByCode(code).map(this::toDomain);
    }

    @Override
    public Optional<Coupon> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Coupon> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<Coupon> findByActive(boolean active) {
        return jpaRepository.findByActive(active).stream().map(this::toDomain).toList();
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    // === Mapping methods ===

    private void updateEntity(CouponEntity entity, Coupon coupon) {
        entity.setDescription(coupon.getDescription());
        entity.setActive(coupon.isActive());
        entity.setUpdatedAt(coupon.getUpdatedAt());

        // Reset rules and re-apply
        entity.setMinimumOrderValue(null);
        entity.setExpiresAt(null);
        entity.setSingleUsePerClient(false);
        entity.setMaxUsages(null);

        for (CouponRule rule : coupon.getRules()) {
            if (rule instanceof MinimumOrderValueRule r) {
                entity.setMinimumOrderValue(r.getMinimumValue());
            } else if (rule instanceof ExpirationDateRule r) {
                entity.setExpiresAt(r.getExpiresAt());
            } else if (rule instanceof SingleUsePerClientRule) {
                entity.setSingleUsePerClient(true);
            } else if (rule instanceof MaxUsageRule r) {
                entity.setMaxUsages(r.getMaxUses());
            }
        }
    }

    private CouponEntity toEntity(Coupon coupon) {
        CouponEntity entity = new CouponEntity();
        entity.setId(coupon.getId());
        entity.setCode(coupon.getCode().getValue());
        entity.setDescription(coupon.getDescription());
        entity.setDiscountType(CouponEntity.DiscountTypeJpa.valueOf(coupon.getDiscountType().name()));
        entity.setDiscountValue(coupon.getDiscountValue());
        entity.setActive(coupon.isActive());
        entity.setCreatedAt(coupon.getCreatedAt());
        entity.setUpdatedAt(coupon.getUpdatedAt());

        // Flatten rules into entity columns
        for (CouponRule rule : coupon.getRules()) {
            if (rule instanceof MinimumOrderValueRule r) {
                entity.setMinimumOrderValue(r.getMinimumValue());
            } else if (rule instanceof ExpirationDateRule r) {
                entity.setExpiresAt(r.getExpiresAt());
            } else if (rule instanceof SingleUsePerClientRule) {
                entity.setSingleUsePerClient(true);
            } else if (rule instanceof MaxUsageRule r) {
                entity.setMaxUsages(r.getMaxUses());
            }
        }

        return entity;
    }

    private Coupon toDomain(CouponEntity entity) {
        List<CouponRule> rules = new ArrayList<>();

        if (entity.getMinimumOrderValue() != null
                && entity.getMinimumOrderValue().compareTo(BigDecimal.ZERO) > 0) {
            rules.add(new MinimumOrderValueRule(entity.getMinimumOrderValue()));
        }
        if (entity.getExpiresAt() != null) {
            rules.add(new ExpirationDateRule(entity.getExpiresAt()));
        }
        if (entity.isSingleUsePerClient()) {
            rules.add(new SingleUsePerClientRule());
        }
        if (entity.getMaxUsages() != null && entity.getMaxUsages() > 0) {
            rules.add(new MaxUsageRule(entity.getMaxUsages()));
        }

        return new Coupon(
                entity.getId(),
                new CouponCode(entity.getCode()),
                entity.getDescription(),
                DiscountType.valueOf(entity.getDiscountType().name()),
                entity.getDiscountValue(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                rules
        );
    }
}
