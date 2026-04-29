package br.com.maxsueleinstein.cuponomia.infrastructure.persistence.entity;

import jakarta.persistence.*;

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

    // Getters e setters para o JPA

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DiscountTypeJpa getDiscountType() {
        return discountType;
    }

    public void setDiscountType(DiscountTypeJpa discountType) {
        this.discountType = discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public BigDecimal getMinimumOrderValue() {
        return minimumOrderValue;
    }

    public void setMinimumOrderValue(BigDecimal minimumOrderValue) {
        this.minimumOrderValue = minimumOrderValue;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isSingleUsePerClient() {
        return singleUsePerClient;
    }

    public void setSingleUsePerClient(boolean singleUsePerClient) {
        this.singleUsePerClient = singleUsePerClient;
    }

    public Integer getMaxUsages() {
        return maxUsages;
    }

    public void setMaxUsages(Integer maxUsages) {
        this.maxUsages = maxUsages;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public enum DiscountTypeJpa {
        FIXED, PERCENTAGE
    }
}
