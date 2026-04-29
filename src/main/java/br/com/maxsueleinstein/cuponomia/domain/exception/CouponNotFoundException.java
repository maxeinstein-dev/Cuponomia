package br.com.maxsueleinstein.cuponomia.domain.exception;

/**
 * Thrown when a coupon is not found by code or ID.
 */
public class CouponNotFoundException extends RuntimeException {
    public CouponNotFoundException(String code) {
        super("Cupom não encontrado: " + code);
    }
}
