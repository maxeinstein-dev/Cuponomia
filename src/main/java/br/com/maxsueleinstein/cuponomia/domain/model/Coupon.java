package br.com.maxsueleinstein.cuponomia.domain.model;

import br.com.maxsueleinstein.cuponomia.domain.rule.CouponRule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Rich domain entity representing a coupon.
 * <p>
 * Encapsulates business logic for:
 * - Discount calculation (fixed or percentage)
 * - Rule validation via Specification Pattern
 * - Lifecycle management (activation/deactivation)
 * <p>
 * Invariants enforced:
 * - Discount value must be positive
 * - Percentage discount must be between 0 (exclusive) and 100 (inclusive)
 * - Code is immutable after creation
 */
public class Coupon {

    private final UUID id;
    private final CouponCode code;
    private final String description;
    private final DiscountType discountType;
    private final BigDecimal discountValue;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<CouponRule> rules;

    public Coupon(UUID id, CouponCode code, String description,
                  DiscountType discountType, BigDecimal discountValue,
                  boolean active, LocalDateTime createdAt, LocalDateTime updatedAt,
                  List<CouponRule> rules) {
        Objects.requireNonNull(id, "Coupon ID must not be null");
        Objects.requireNonNull(code, "Coupon code must not be null");
        Objects.requireNonNull(discountType, "Discount type must not be null");
        Objects.requireNonNull(discountValue, "Discount value must not be null");

        if (discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount value must be positive");
        }
        if (discountType == DiscountType.PERCENTAGE && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Percentage discount must not exceed 100%");
        }

        this.id = id;
        this.code = code;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.rules = rules != null ? new ArrayList<>(rules) : new ArrayList<>();
    }

    /**
     * Validates this coupon against all its rules using the given checkout context.
     * All rules are evaluated, accumulating every violation for clear user feedback.
     */
    public ValidationResult validate(CheckoutContext context) {
        if (!active) {
            return ValidationResult.fail("O cupom '" + code.getValue() + "' não está ativo");
        }

        ValidationResult result = ValidationResult.ok();
        for (CouponRule rule : rules) {
            result = result.combine(rule.validate(this, context));
        }
        return result;
    }

    /**
     * Calculates the discount amount for the given order total.
     * <p>
     * For FIXED: returns the discount value directly (capped at order total).
     * For PERCENTAGE: returns (orderTotal * discountValue / 100), rounded to 2 decimal places.
     * The result is always capped at the order total to prevent negative totals.
     */
    public BigDecimal applyDiscount(BigDecimal orderTotal) {
        Objects.requireNonNull(orderTotal, "Order total must not be null");

        BigDecimal discount = switch (discountType) {
            case FIXED -> discountValue;
            case PERCENTAGE -> orderTotal
                    .multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        };

        // Cap discount at order total (never go negative)
        return discount.min(orderTotal);
    }

    /**
     * Deactivates this coupon, preventing further use.
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void addRule(CouponRule rule) {
        Objects.requireNonNull(rule, "Rule must not be null");
        this.rules.add(rule);
    }

    // Getters (no setters — rich entity with controlled mutation)

    public UUID getId() {
        return id;
    }

    public CouponCode getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<CouponRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coupon coupon = (Coupon) o;
        return Objects.equals(id, coupon.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Coupon{code=" + code + ", type=" + discountType + ", value=" + discountValue + ", active=" + active + "}";
    }
}
