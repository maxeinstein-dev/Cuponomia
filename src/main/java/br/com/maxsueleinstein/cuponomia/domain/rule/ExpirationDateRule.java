package br.com.maxsueleinstein.cuponomia.domain.rule;

import br.com.maxsueleinstein.cuponomia.domain.model.CheckoutContext;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.model.ValidationResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Regra: o cupom não pode estar expirado.
 * 
 * Compara o momento atual com a data de expiração do cupom.
 * (O uso de relógio injetado pode ser adotado para facilitar os testes).
 */
public class ExpirationDateRule implements CouponRule {

    private final LocalDateTime expiresAt;

    public ExpirationDateRule(LocalDateTime expiresAt) {
        Objects.requireNonNull(expiresAt, "A data de expiração não pode ser nula");
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
