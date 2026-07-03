package br.com.maxsueleinstein.cuponomia.domain.rule;

import br.com.maxsueleinstein.cuponomia.domain.model.CheckoutContext;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.model.ValidationResult;

/**
 * Regra: o cupom tem um número máximo de usos totais (entre todos os clientes).
 * 
 * Demonstra extensibilidade — uma regra não presente nos requisitos originais
 * mas adicionada trivialmente graças ao Specification Pattern.
 */
public class MaxUsageRule implements CouponRule {

    private final int maxUses;

    public MaxUsageRule(int maxUses) {
        if (maxUses <= 0) {
            throw new IllegalArgumentException("O máximo de usos deve ser positivo");
        }
        this.maxUses = maxUses;
    }

    @Override
    public ValidationResult validate(Coupon coupon, CheckoutContext context) {
        if (context.getTotalCouponUsages() >= maxUses) {
            return ValidationResult.fail(
                    String.format("O cupom '%s' atingiu o limite máximo de %d usos",
                            coupon.getCode().getValue(), maxUses));
        }
        return ValidationResult.ok();
    }

    @Override
    public String description() {
        return String.format("Máximo de %d usos totais", maxUses);
    }

    public int getMaxUses() {
        return maxUses;
    }
}
