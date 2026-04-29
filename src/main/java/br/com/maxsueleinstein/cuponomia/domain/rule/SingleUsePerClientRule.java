package br.com.maxsueleinstein.cuponomia.domain.rule;

import br.com.maxsueleinstein.cuponomia.domain.model.CheckoutContext;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.model.ValidationResult;

/**
 * Rule: each client may use this coupon only once.
 * <p>
 * The CheckoutContext carries a pre-loaded flag indicating whether
 * the client has already used this coupon. This keeps the domain
 * layer pure — no repository access needed here.
 */
public class SingleUsePerClientRule implements CouponRule {

    @Override
    public ValidationResult validate(Coupon coupon, CheckoutContext context) {
        if (context.hasClientAlreadyUsedCoupon()) {
            return ValidationResult.fail(
                    String.format("O cliente '%s' já utilizou o cupom '%s'",
                            context.getClientId(), coupon.getCode().getValue())
            );
        }
        return ValidationResult.ok();
    }

    @Override
    public String description() {
        return "Uso único por cliente";
    }
}
