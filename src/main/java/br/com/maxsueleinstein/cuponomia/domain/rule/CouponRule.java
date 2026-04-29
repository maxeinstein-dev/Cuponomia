package br.com.maxsueleinstein.cuponomia.domain.rule;

import br.com.maxsueleinstein.cuponomia.domain.model.CheckoutContext;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.model.ValidationResult;

/**
 * Specification Pattern interface for coupon validation rules.
 * <p>
 * Each implementation encapsulates a single validation concern.
 * Rules are composable — a coupon can have multiple rules that are
 * all evaluated to provide comprehensive feedback.
 */
public interface CouponRule {

    /**
     * Validates the coupon against this rule in the given checkout context.
     *
     * @param coupon  the coupon being validated
     * @param context the checkout context with order and client information
     * @return ValidationResult.ok() if the rule passes, or ValidationResult.fail() with a message
     */
    ValidationResult validate(Coupon coupon, CheckoutContext context);

    /**
     * Returns a human-readable description of this rule.
     */
    String description();
}
