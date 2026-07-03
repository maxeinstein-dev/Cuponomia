package br.com.maxsueleinstein.cuponomia.domain.exception;

/**
 * Lançada quando um cliente tenta reusar um cupom que ele já utilizou.
 */
public class CouponAlreadyUsedException extends RuntimeException {
    public CouponAlreadyUsedException(String couponCode, String clientId) {
        super("O cupom '" + couponCode + "' já foi utilizado pelo cliente '" + clientId + "'");
    }
}
