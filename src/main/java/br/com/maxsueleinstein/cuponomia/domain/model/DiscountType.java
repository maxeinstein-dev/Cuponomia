package br.com.maxsueleinstein.cuponomia.domain.model;

/**
 * Enum representing the type of discount a coupon provides.
 * <p>
 * FIXED: A fixed monetary amount is subtracted from the order total (e.g., R$ 20.00 off).
 * PERCENTAGE: A percentage of the order total is subtracted (e.g., 15% off).
 */
public enum DiscountType {
    FIXED,
    PERCENTAGE
}
