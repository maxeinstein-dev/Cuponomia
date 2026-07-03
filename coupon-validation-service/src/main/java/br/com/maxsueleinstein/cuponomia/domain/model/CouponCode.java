package br.com.maxsueleinstein.cuponomia.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object representing a coupon code.
 * 
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
            throw new IllegalArgumentException("O código do cupom não pode ser nulo ou vazio");
        }
        String normalized = value.trim().toUpperCase();
        if (!VALID_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "O código do cupom deve conter de 3 a 30 caracteres alfanuméricos (hifens e underlines permitidos). Recebido: "
                            + value);
        }
        this.value = normalized;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
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
