package br.com.maxsueleinstein.cuponomia.domain.exception;

/**
 * Thrown when attempting to create a coupon with a code that already exists.
 */
public class DuplicateCouponCodeException extends RuntimeException {
    public DuplicateCouponCodeException(String code) {
        super("Já existe um cupom com o código '" + code + "'");
    }
}
