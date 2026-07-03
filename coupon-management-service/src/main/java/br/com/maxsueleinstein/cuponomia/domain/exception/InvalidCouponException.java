package br.com.maxsueleinstein.cuponomia.domain.exception;

import java.util.List;

/**
 * Lançada quando a validação do cupom falha (uma ou mais regras violadas).
 * Carrega todas as mensagens de erro de validação para fornecer um feedback claro ao usuário.
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
