package br.com.maxsueleinstein.cuponomia.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Value Object representing the result of coupon validation.
 * <p>
 * Composable: multiple ValidationResults can be combined into one,
 * accumulating all error messages. This supports the Specification Pattern
 * where multiple rules are evaluated and all violations are reported.
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
     * Combines two validation results. If either is invalid, the combined
     * result is invalid with all errors accumulated.
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
