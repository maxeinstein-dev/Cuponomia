package br.com.maxsueleinstein.cuponomia.domain.rule;

import br.com.maxsueleinstein.cuponomia.domain.model.CheckoutContext;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.model.ValidationResult;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Objects;

/**
 * Regra: o total do pedido deve ser pelo menos um valor mínimo.
 * 
 * Evita que cupons sejam aplicados em pedidos de valor muito baixo.
 */
public class MinimumOrderValueRule implements CouponRule {

    private final BigDecimal minimumValue;

    public MinimumOrderValueRule(BigDecimal minimumValue) {
        Objects.requireNonNull(minimumValue, "O valor mínimo não pode ser nulo");
        if (minimumValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor mínimo deve ser positivo");
        }
        this.minimumValue = minimumValue;
    }

    @Override
    public ValidationResult validate(Coupon coupon, CheckoutContext context) {
        if (context.getOrderTotal().compareTo(minimumValue) < 0) {
            return ValidationResult.fail(
                    String.format(Locale.US, "O valor do pedido R$ %.2f está abaixo do mínimo exigido de R$ %.2f",
                            context.getOrderTotal(), minimumValue));
        }
        return ValidationResult.ok();
    }

    @Override
    public String description() {
        return String.format(Locale.US, "Valor mínimo do pedido de R$ %.2f", minimumValue);
    }

    public BigDecimal getMinimumValue() {
        return minimumValue;
    }
}
