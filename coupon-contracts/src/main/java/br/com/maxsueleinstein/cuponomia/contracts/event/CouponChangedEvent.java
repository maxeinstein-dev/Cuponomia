package br.com.maxsueleinstein.cuponomia.contracts.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record CouponChangedEvent(
        UUID eventId,
        CouponEventType eventType,
        Instant occurredAt,
        UUID couponId,
        String code,
        String description,
        String discountType,
        BigDecimal discountValue,
        boolean active,
        RulesPayload rules,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public record RulesPayload(
            BigDecimal minimumOrderValue,
            LocalDateTime expiresAt,
            boolean singleUsePerClient,
            Integer maxUsages) {
    }
}
