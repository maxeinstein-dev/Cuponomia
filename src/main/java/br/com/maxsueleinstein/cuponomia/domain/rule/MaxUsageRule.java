package br.com.maxsueleinstein.cuponomia.domain.rule;

import br.com.maxsueleinstein.cuponomia.domain.model.CheckoutContext;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.model.ValidationResult;

/**
 * Rule: the coupon has a maximum number of total usages across all clients.
 * <p>
 * Demonstrates extensibility — a rule not in the original requirements
 * but trivially added thanks to the Specification Pattern.
 */
public class MaxUsageRule implements CouponRule {

    private final int maxUses;

    public MaxUsageRule(int maxUses) {
        if (maxUses <= 0) {
            throw new IllegalArgumentException("Max uses must be positive");
        }
        this.maxUses = maxUses;
    }

    @Override
    public ValidationResult validate(Coupon coupon, CheckoutContext context) {
        if (context.getTotalCouponUsages() >= maxUses) {
            return ValidationResult.fail(
                    String.format("O cupom '%s' atingiu o limite máximo de %d usos",
                            coupon.getCode().getValue(), maxUses)
            );
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
