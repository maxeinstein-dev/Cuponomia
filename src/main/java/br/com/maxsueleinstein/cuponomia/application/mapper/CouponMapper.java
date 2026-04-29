package br.com.maxsueleinstein.cuponomia.application.mapper;

import br.com.maxsueleinstein.cuponomia.application.dto.CouponResponse;
import br.com.maxsueleinstein.cuponomia.application.dto.CreateCouponRequest;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.model.CouponCode;
import br.com.maxsueleinstein.cuponomia.domain.model.DiscountType;
import br.com.maxsueleinstein.cuponomia.domain.rule.CouponRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.ExpirationDateRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.MaxUsageRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.MinimumOrderValueRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.SingleUsePerClientRule;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Maps between domain models and DTOs.
 * 
 * Centralizes all conversion logic to keep controllers and use cases clean.
 */
@Component
public class CouponMapper {

    /**
     * Converts a CreateCouponRequest DTO into a domain Coupon entity.
     */
    public Coupon toDomain(CreateCouponRequest request) {
        DiscountType type = DiscountType.valueOf(request.discountType().toUpperCase());

        List<CouponRule> rules = buildRules(request.rules());

        return new Coupon(
                UUID.randomUUID(),
                new CouponCode(request.code()),
                request.description(),
                type,
                request.discountValue(),
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                rules);
    }

    /**
     * Converts a domain Coupon entity into a CouponResponse DTO.
     */
    public CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode().getValue(),
                coupon.getDescription(),
                coupon.getDiscountType().name(),
                coupon.getDiscountValue(),
                coupon.isActive(),
                buildRulesResponse(coupon.getRules()),
                coupon.getCreatedAt(),
                coupon.getUpdatedAt());
    }

    private List<CouponRule> buildRules(CreateCouponRequest.RulesRequest rulesRequest) {
        List<CouponRule> rules = new ArrayList<>();
        if (rulesRequest == null)
            return rules;

        if (rulesRequest.minimumOrderValue() != null
                && rulesRequest.minimumOrderValue().compareTo(BigDecimal.ZERO) > 0) {
            rules.add(new MinimumOrderValueRule(rulesRequest.minimumOrderValue()));
        }
        if (rulesRequest.expiresAt() != null) {
            rules.add(new ExpirationDateRule(rulesRequest.expiresAt()));
        }
        if (Boolean.TRUE.equals(rulesRequest.singleUsePerClient())) {
            rules.add(new SingleUsePerClientRule());
        }
        if (rulesRequest.maxUsages() != null && rulesRequest.maxUsages() > 0) {
            rules.add(new MaxUsageRule(rulesRequest.maxUsages()));
        }

        return rules;
    }

    private CouponResponse.RulesResponse buildRulesResponse(List<CouponRule> rules) {
        BigDecimal minimumOrderValue = null;
        LocalDateTime expiresAt = null;
        Boolean singleUsePerClient = null;
        Integer maxUsages = null;

        for (CouponRule rule : rules) {
            if (rule instanceof MinimumOrderValueRule r) {
                minimumOrderValue = r.getMinimumValue();
            } else if (rule instanceof ExpirationDateRule r) {
                expiresAt = r.getExpiresAt();
            } else if (rule instanceof SingleUsePerClientRule) {
                singleUsePerClient = true;
            } else if (rule instanceof MaxUsageRule r) {
                maxUsages = r.getMaxUses();
            }
        }

        return new CouponResponse.RulesResponse(minimumOrderValue, expiresAt, singleUsePerClient, maxUsages);
    }
}
