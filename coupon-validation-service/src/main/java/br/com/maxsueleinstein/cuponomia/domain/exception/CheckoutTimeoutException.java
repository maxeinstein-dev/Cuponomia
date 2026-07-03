package br.com.maxsueleinstein.cuponomia.domain.exception;

public class CheckoutTimeoutException extends RuntimeException {

    public CheckoutTimeoutException(String message) {
        super(message);
    }
}