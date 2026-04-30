package br.com.maxsueleinstein.cuponomia.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade JPA para os cupons.
 * 
 * Separada da entidade de domínio Coupon para evitar que anotações JPA
 * vazem para a camada de domínio (princípio da Arquitetura Hexagonal).
 */
@Entity
@Getter
@Setter
@Table(name = "coupons")
public class CouponEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "code", unique = true, nullable = false, length = 30)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    private DiscountTypeJpa discountType;

    @Column(name = "discount_value", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "active", nullable = false)
    private boolean active;

    // Campos de regras armazenados como colunas (simples e fáceis de consultar)
    @Column(name = "minimum_order_value", precision = 12, scale = 2)
    private BigDecimal minimumOrderValue;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "single_use_per_client")
    private boolean singleUsePerClient;

    @Column(name = "max_usages")
    private Integer maxUsages;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;

    public CouponEntity() {
    }

    public enum DiscountTypeJpa {
        FIXED, PERCENTAGE
    }
}