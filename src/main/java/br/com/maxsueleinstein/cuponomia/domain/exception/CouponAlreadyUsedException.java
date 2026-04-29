package br.com.maxsueleinstein.cuponomia.domain.exception;

/**
 * Thrown when a client attempts to reuse a coupon they have already used.
 */
public class CouponAlreadyUsedException extends RuntimeException {
    public CouponAlreadyUsedException(String couponCode, String clientId) {
        super("O cupom '" + couponCode + "' já foi utilizado pelo cliente '" + clientId + "'");
    }
}
