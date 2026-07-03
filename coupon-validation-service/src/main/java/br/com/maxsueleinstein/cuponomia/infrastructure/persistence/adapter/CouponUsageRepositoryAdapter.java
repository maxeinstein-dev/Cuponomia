package br.com.maxsueleinstein.cuponomia.infrastructure.persistence.adapter;

import br.com.maxsueleinstein.cuponomia.domain.model.CouponUsage;
import br.com.maxsueleinstein.cuponomia.domain.repository.CouponUsageRepository;
import br.com.maxsueleinstein.cuponomia.infrastructure.persistence.entity.CouponUsageEntity;
import br.com.maxsueleinstein.cuponomia.infrastructure.persistence.jpa.JpaCouponUsageRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Adapter that implements the domain CouponUsageRepository port using Spring Data JPA.
 */
@Component
public class CouponUsageRepositoryAdapter implements CouponUsageRepository {

    private final JpaCouponUsageRepository jpaRepository;

    public CouponUsageRepositoryAdapter(JpaCouponUsageRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CouponUsage save(CouponUsage usage) {
        CouponUsageEntity entity = toEntity(usage);
        CouponUsageEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public boolean existsByCouponIdAndClientId(UUID couponId, String clientId) {
        return jpaRepository.existsByCouponIdAndClientId(couponId, clientId);
    }

    @Override
    public long countByCouponId(UUID couponId) {
        return jpaRepository.countByCouponId(couponId);
    }

    @Override
    public List<CouponUsage> findByCouponId(UUID couponId) {
        return jpaRepository.findByCouponId(couponId).stream()
                .map(this::toDomain)
                .toList();
    }

    private CouponUsageEntity toEntity(CouponUsage usage) {
        CouponUsageEntity entity = new CouponUsageEntity();
        entity.setId(usage.getId());
        entity.setCouponId(usage.getCouponId());
        entity.setClientId(usage.getClientId());
        entity.setOrderTotal(usage.getOrderTotal());
        entity.setDiscountApplied(usage.getDiscountApplied());
        entity.setUsedAt(usage.getUsedAt());
        return entity;
    }

    private CouponUsage toDomain(CouponUsageEntity entity) {
        return new CouponUsage(
                entity.getId(),
                entity.getCouponId(),
                entity.getClientId(),
                entity.getOrderTotal(),
                entity.getDiscountApplied(),
                entity.getUsedAt()
        );
    }
}
