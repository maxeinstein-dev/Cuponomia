package br.com.maxsueleinstein.cuponomia.domain.exception;

/**
 * Lançada ao tentar criar um cupom com um código que já existe.
 */
public class DuplicateCouponCodeException extends RuntimeException {
    public DuplicateCouponCodeException(String code) {
        super("Já existe um cupom com o código '" + code + "'");
    }
}
