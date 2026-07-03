package br.com.maxsueleinstein.cuponomia.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Objeto de Valor (Value Object) que representa o resultado da validação de um
 * cupom.
 * 
 * Componível: múltiplos ValidationResults podem ser combinados em um só,
 * acumulando todas as mensagens de erro. Isso suporta o Specification Pattern
 * onde múltiplas regras são avaliadas e todas as violações são reportadas.
 */
public final class ValidationResult {

    private final boolean valid;
    private final List<String> errors;

    private ValidationResult(boolean valid, List<String> errors) {
        this.valid = valid;
        this.errors = Collections.unmodifiableList(errors);
    }

    public static ValidationResult ok() {
        return new ValidationResult(true, List.of());
    }

    public static ValidationResult fail(String error) {
        return new ValidationResult(false, List.of(error));
    }

    /**
     * Combina dois resultados de validação. Se qualquer um for inválido, o
     * resultado
     * combinado será inválido com todos os erros acumulados.
     */
    public ValidationResult combine(ValidationResult other) {
        if (this.valid && other.valid) {
            return ok();
        }
        List<String> combinedErrors = new ArrayList<>(this.errors);
        combinedErrors.addAll(other.errors);
        return new ValidationResult(false, combinedErrors);
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return valid ? "ValidationResult[OK]" : "ValidationResult[FAIL: " + errors + "]";
    }
}
