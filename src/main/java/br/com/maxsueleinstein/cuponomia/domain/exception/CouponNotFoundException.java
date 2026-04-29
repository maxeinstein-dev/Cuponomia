package br.com.maxsueleinstein.cuponomia.domain.exception;

/**
 * Lançada quando um cupom não é encontrado por código ou ID.
 */
public class CouponNotFoundException extends RuntimeException {
    public CouponNotFoundException(String code) {
        super("Cupom não encontrado: " + code);
    }
}
