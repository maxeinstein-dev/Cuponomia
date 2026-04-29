package br.com.maxsueleinstein.cuponomia.domain.rule;

import br.com.maxsueleinstein.cuponomia.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Coupon Rules (Specification Pattern)")
class CouponRulesTest {

    private final Coupon coupon = new Coupon(
            UUID.randomUUID(), new CouponCode("TEST"), "Test",
            DiscountType.FIXED, BigDecimal.TEN, true,
            LocalDateTime.now(), LocalDateTime.now(), List.of()
    );

    @Nested
    @DisplayName("MinimumOrderValueRule")
    class MinimumOrderValueRuleTest {

        private final MinimumOrderValueRule rule = new MinimumOrderValueRule(new BigDecimal("100.00"));

        @Test
        @DisplayName("should pass when order meets minimum")
        void shouldPassWhenMeetsMinimum() {
            CheckoutContext ctx = new CheckoutContext("c1", new BigDecimal("100.00"), false, 0);
            assertTrue(rule.validate(coupon, ctx).isValid());
        }

        @Test
        @DisplayName("should pass when order exceeds minimum")
        void shouldPassWhenExceedsMinimum() {
            CheckoutContext ctx = new CheckoutContext("c1", new BigDecimal("200.00"), false, 0);
            assertTrue(rule.validate(coupon, ctx).isValid());
        }

        @Test
        @DisplayName("should fail when order below minimum")
        void shouldFailWhenBelowMinimum() {
            CheckoutContext ctx = new CheckoutContext("c1", new BigDecimal("50.00"), false, 0);
            ValidationResult result = rule.validate(coupon, ctx);
            assertFalse(result.isValid());
            String error = result.getErrors().get(0).toLowerCase();
            assertTrue(error.contains("abaixo") || error.contains("mínimo"),
                    "Error should mention abaixo/mínimo. Got: " + error);
        }

        @Test
        @DisplayName("should reject non-positive minimum value")
        void shouldRejectNonPositiveMinimum() {
            assertThrows(IllegalArgumentException.class, () -> new MinimumOrderValueRule(BigDecimal.ZERO));
        }
    }

    @Nested
    @DisplayName("ExpirationDateRule")
    class ExpirationDateRuleTest {

        @Test
        @DisplayName("should pass when coupon is not expired")
        void shouldPassWhenNotExpired() {
            ExpirationDateRule rule = new ExpirationDateRule(LocalDateTime.now().plusDays(30));
            CheckoutContext ctx = new CheckoutContext("c1", BigDecimal.TEN, false, 0);
            assertTrue(rule.validate(coupon, ctx).isValid());
        }

        @Test
        @DisplayName("should fail when coupon is expired")
        void shouldFailWhenExpired() {
            ExpirationDateRule rule = new ExpirationDateRule(LocalDateTime.now().minusDays(1));
            CheckoutContext ctx = new CheckoutContext("c1", BigDecimal.TEN, false, 0);
            ValidationResult result = rule.validate(coupon, ctx);
            assertFalse(result.isValid());
            assertTrue(result.getErrors().get(0).contains("expirou"));
        }

        @Test
        @DisplayName("should reject null expiration date")
        void shouldRejectNullDate() {
            assertThrows(NullPointerException.class, () -> new ExpirationDateRule(null));
        }
    }

    @Nested
    @DisplayName("SingleUsePerClientRule")
    class SingleUsePerClientRuleTest {

        private final SingleUsePerClientRule rule = new SingleUsePerClientRule();

        @Test
        @DisplayName("should pass when client has not used coupon")
        void shouldPassWhenNotUsed() {
            CheckoutContext ctx = new CheckoutContext("c1", BigDecimal.TEN, false, 0);
            assertTrue(rule.validate(coupon, ctx).isValid());
        }

        @Test
        @DisplayName("should fail when client has already used coupon")
        void shouldFailWhenAlreadyUsed() {
            CheckoutContext ctx = new CheckoutContext("c1", BigDecimal.TEN, true, 0);
            ValidationResult result = rule.validate(coupon, ctx);
            assertFalse(result.isValid());
            assertTrue(result.getErrors().get(0).contains("já utilizou"));
        }
    }

    @Nested
    @DisplayName("MaxUsageRule")
    class MaxUsageRuleTest {

        private final MaxUsageRule rule = new MaxUsageRule(100);

        @Test
        @DisplayName("should pass when below max usages")
        void shouldPassWhenBelowMax() {
            CheckoutContext ctx = new CheckoutContext("c1", BigDecimal.TEN, false, 50);
            assertTrue(rule.validate(coupon, ctx).isValid());
        }

        @Test
        @DisplayName("should fail when at max usages")
        void shouldFailWhenAtMax() {
            CheckoutContext ctx = new CheckoutContext("c1", BigDecimal.TEN, false, 100);
            ValidationResult result = rule.validate(coupon, ctx);
            assertFalse(result.isValid());
            assertTrue(result.getErrors().get(0).contains("limite máximo"));
        }

        @Test
        @DisplayName("should fail when above max usages")
        void shouldFailWhenAboveMax() {
            CheckoutContext ctx = new CheckoutContext("c1", BigDecimal.TEN, false, 101);
            assertFalse(rule.validate(coupon, ctx).isValid());
        }

        @Test
        @DisplayName("should reject non-positive max uses")
        void shouldRejectNonPositiveMax() {
            assertThrows(IllegalArgumentException.class, () -> new MaxUsageRule(0));
            assertThrows(IllegalArgumentException.class, () -> new MaxUsageRule(-1));
        }
    }
}
