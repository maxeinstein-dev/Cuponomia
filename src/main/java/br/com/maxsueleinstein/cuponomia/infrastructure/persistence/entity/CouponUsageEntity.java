package br.com.maxsueleinstein.cuponomia.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity for coupon usage records.
 * 
 * UNIQUE constraint on (coupon_id, client_id) enforces single-use-per-client
 * at the database level as a safety net against race conditions.
 */
@Entity
@Getter
@Setter
@Table(name = "coupon_usages", uniqueConstraints = @UniqueConstraint(name = "uk_coupon_usage_coupon_client", columnNames = {
        "coupon_id", "client_id" }))
public class CouponUsageEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "coupon_id", nullable = false)
    private UUID couponId;

    @Column(name = "client_id", nullable = false, length = 100)
    private String clientId;

    @Column(name = "order_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal orderTotal;

    @Column(name = "discount_applied", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountApplied;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;

    public CouponUsageEntity() {
    }
}