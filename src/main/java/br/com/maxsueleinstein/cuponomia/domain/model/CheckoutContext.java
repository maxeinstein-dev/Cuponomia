package br.com.maxsueleinstein.cuponomia.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object representing the context of a checkout operation.
 * <p>
 * Contains all information needed by coupon rules to validate
 * whether a coupon can be applied. Enriched by the Use Case layer
 * before being passed to the domain for validation.
 */
public final class CheckoutContext {

    private final String clientId;
    private final BigDecimal orderTotal;
    private final boolean clientAlreadyUsedCoupon;
    private final long totalCouponUsages;

    public CheckoutContext(String clientId, BigDecimal orderTotal,
                           boolean clientAlreadyUsedCoupon, long totalCouponUsages) {
        Objects.requireNonNull(clientId, "Client ID must not be null");
        Objects.requireNonNull(orderTotal, "Order total must not be null");
        if (orderTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Order total must not be negative");
        }
        this.clientId = clientId;
        this.orderTotal = orderTotal;
        this.clientAlreadyUsedCoupon = clientAlreadyUsedCoupon;
        this.totalCouponUsages = totalCouponUsages;
    }

    public String getClientId() {
        return clientId;
    }

    public BigDecimal getOrderTotal() {
        return orderTotal;
    }

    public boolean hasClientAlreadyUsedCoupon() {
        return clientAlreadyUsedCoupon;
    }

    public long getTotalCouponUsages() {
        return totalCouponUsages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CheckoutContext that = (CheckoutContext) o;
        return clientAlreadyUsedCoupon == that.clientAlreadyUsedCoupon
                && totalCouponUsages == that.totalCouponUsages
                && Objects.equals(clientId, that.clientId)
                && Objects.equals(orderTotal, that.orderTotal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, orderTotal, clientAlreadyUsedCoupon, totalCouponUsages);
    }
}
