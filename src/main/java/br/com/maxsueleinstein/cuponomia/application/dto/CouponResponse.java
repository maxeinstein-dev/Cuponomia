package br.com.maxsueleinstein.cuponomia.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO representing a coupon's public data.
 */
public record CouponResponse(
        UUID id,
        String code,
        String description,
        String discountType,
        BigDecimal discountValue,
        boolean active,
        RulesResponse rules,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record RulesResponse(
            BigDecimal minimumOrderValue,
            LocalDateTime expiresAt,
            Boolean singleUsePerClient,
            Integer maxUsages
    ) {}
}
