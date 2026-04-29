package br.com.maxsueleinstein.cuponomia.domain.rule;

import br.com.maxsueleinstein.cuponomia.domain.model.CheckoutContext;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.model.ValidationResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Rule: the coupon must not have expired.
 * <p>
 * Compares the current time against the coupon's expiration date.
 * Uses an injected clock supplier for testability.
 */
public class ExpirationDateRule implements CouponRule {

    private final LocalDateTime expiresAt;

    public ExpirationDateRule(LocalDateTime expiresAt) {
        Objects.requireNonNull(expiresAt, "Expiration date must not be null");
        this.expiresAt = expiresAt;
    }

    @Override
    public ValidationResult validate(Coupon coupon, CheckoutContext context) {
        if (LocalDateTime.now().isAfter(expiresAt)) {
            return ValidationResult.fail(
                    String.format("O cupom expirou em %s",
                            expiresAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        }
        return ValidationResult.ok();
    }

    @Override
    public String description() {
        return String.format("Expira em %s",
                expiresAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}
