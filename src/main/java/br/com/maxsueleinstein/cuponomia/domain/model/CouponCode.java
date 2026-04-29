package br.com.maxsueleinstein.cuponomia.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing a coupon code.
 * <p>
 * Enforces business invariants:
 * - Not null or blank
 * - Alphanumeric with optional hyphens/underscores
 * - Between 3 and 30 characters
 * - Always stored in uppercase
 */
public final class CouponCode {

    private static final Pattern VALID_PATTERN = Pattern.compile("^[A-Z0-9_-]{3,30}$");
    private final String value;

    public CouponCode(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Coupon code must not be null or blank");
        }
        String normalized = value.trim().toUpperCase();
        if (!VALID_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Coupon code must be 3-30 alphanumeric characters (hyphens and underscores allowed). Got: " + value
            );
        }
        this.value = normalized;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CouponCode that = (CouponCode) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
