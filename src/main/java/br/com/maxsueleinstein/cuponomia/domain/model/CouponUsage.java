package br.com.maxsueleinstein.cuponomia.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a record of coupon usage.
 * <p>
 * Tracks who used a coupon, when, and how much discount was applied.
 * Used for auditing and enforcing single-use-per-client rules.
 */
public class CouponUsage {

    private final UUID id;
    private final UUID couponId;
    private final String clientId;
    private final BigDecimal orderTotal;
    private final BigDecimal discountApplied;
    private final LocalDateTime usedAt;

    public CouponUsage(UUID id, UUID couponId, String clientId,
                       BigDecimal orderTotal, BigDecimal discountApplied,
                       LocalDateTime usedAt) {
        Objects.requireNonNull(id, "Usage ID must not be null");
        Objects.requireNonNull(couponId, "Coupon ID must not be null");
        Objects.requireNonNull(clientId, "Client ID must not be null");
        Objects.requireNonNull(orderTotal, "Order total must not be null");
        Objects.requireNonNull(discountApplied, "Discount applied must not be null");

        this.id = id;
        this.couponId = couponId;
        this.clientId = clientId;
        this.orderTotal = orderTotal;
        this.discountApplied = discountApplied;
        this.usedAt = usedAt != null ? usedAt : LocalDateTime.now();
    }

    public static CouponUsage create(UUID couponId, String clientId,
                                     BigDecimal orderTotal, BigDecimal discountApplied) {
        return new CouponUsage(UUID.randomUUID(), couponId, clientId,
                orderTotal, discountApplied, LocalDateTime.now());
    }

    public UUID getId() {
        return id;
    }

    public UUID getCouponId() {
        return couponId;
    }

    public String getClientId() {
        return clientId;
    }

    public BigDecimal getOrderTotal() {
        return orderTotal;
    }

    public BigDecimal getDiscountApplied() {
        return discountApplied;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CouponUsage that = (CouponUsage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
