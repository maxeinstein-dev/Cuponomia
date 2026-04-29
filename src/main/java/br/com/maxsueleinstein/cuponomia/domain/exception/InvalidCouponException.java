package br.com.maxsueleinstein.cuponomia.domain.exception;

import java.util.List;

/**
 * Thrown when coupon validation fails (one or more rules violated).
 * Carries all validation error messages for clear user feedback.
 */
public class InvalidCouponException extends RuntimeException {

    private final List<String> errors;

    public InvalidCouponException(List<String> errors) {
        super("Falha na validação do cupom: " + String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
