package br.com.maxsueleinstein.cuponomia.domain.model;

import br.com.maxsueleinstein.cuponomia.domain.rule.ExpirationDateRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.MinimumOrderValueRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.SingleUsePerClientRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Coupon Entity")
class CouponTest {

    private Coupon createCoupon(DiscountType type, BigDecimal value, List<br.com.maxsueleinstein.cuponomia.domain.rule.CouponRule> rules) {
        return new Coupon(
                UUID.randomUUID(),
                new CouponCode("TEST10"),
                "Test coupon",
                type,
                value,
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                rules
        );
    }

    @Nested
    @DisplayName("Construction invariants")
    class ConstructionInvariants {

        @Test
        @DisplayName("should reject zero discount value")
        void shouldRejectZeroDiscount() {
            assertThrows(IllegalArgumentException.class, () ->
                    createCoupon(DiscountType.FIXED, BigDecimal.ZERO, List.of()));
        }

        @Test
        @DisplayName("should reject negative discount value")
        void shouldRejectNegativeDiscount() {
            assertThrows(IllegalArgumentException.class, () ->
                    createCoupon(DiscountType.FIXED, new BigDecimal("-10"), List.of()));
        }

        @Test
        @DisplayName("should reject percentage above 100")
        void shouldRejectPercentageAbove100() {
            assertThrows(IllegalArgumentException.class, () ->
                    createCoupon(DiscountType.PERCENTAGE, new BigDecimal("101"), List.of()));
        }

        @Test
        @DisplayName("should accept percentage of exactly 100")
        void shouldAcceptPercentage100() {
            assertDoesNotThrow(() ->
                    createCoupon(DiscountType.PERCENTAGE, new BigDecimal("100"), List.of()));
        }

        @Test
        @DisplayName("should reject null code")
        void shouldRejectNullCode() {
            assertThrows(NullPointerException.class, () ->
                    new Coupon(UUID.randomUUID(), null, "desc", DiscountType.FIXED,
                            BigDecimal.TEN, true, null, null, null));
        }
    }

    @Nested
    @DisplayName("Discount calculation")
    class DiscountCalculation {

        @Test
        @DisplayName("FIXED: should return discount value")
        void fixedShouldReturnValue() {
            Coupon coupon = createCoupon(DiscountType.FIXED, new BigDecimal("20.00"), List.of());
            BigDecimal discount = coupon.applyDiscount(new BigDecimal("100.00"));
            assertEquals(0, new BigDecimal("20.00").compareTo(discount));
        }

        @Test
        @DisplayName("FIXED: should cap at order total")
        void fixedShouldCapAtOrderTotal() {
            Coupon coupon = createCoupon(DiscountType.FIXED, new BigDecimal("50.00"), List.of());
            BigDecimal discount = coupon.applyDiscount(new BigDecimal("30.00"));
            assertEquals(0, new BigDecimal("30.00").compareTo(discount));
        }

        @Test
        @DisplayName("PERCENTAGE: should calculate correctly")
        void percentageShouldCalculate() {
            Coupon coupon = createCoupon(DiscountType.PERCENTAGE, new BigDecimal("15"), List.of());
            BigDecimal discount = coupon.applyDiscount(new BigDecimal("200.00"));
            assertEquals(0, new BigDecimal("30.00").compareTo(discount));
        }

        @Test
        @DisplayName("PERCENTAGE 100%: should return full order total")
        void percentage100ShouldReturnFullTotal() {
            Coupon coupon = createCoupon(DiscountType.PERCENTAGE, new BigDecimal("100"), List.of());
            BigDecimal discount = coupon.applyDiscount(new BigDecimal("250.00"));
            assertEquals(0, new BigDecimal("250.00").compareTo(discount));
        }

        @Test
        @DisplayName("PERCENTAGE: should round to 2 decimal places")
        void percentageShouldRound() {
            Coupon coupon = createCoupon(DiscountType.PERCENTAGE, new BigDecimal("33.33"), List.of());
            BigDecimal discount = coupon.applyDiscount(new BigDecimal("100.00"));
            assertEquals(2, discount.scale());
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("should fail if coupon is inactive")
        void shouldFailIfInactive() {
            Coupon coupon = createCoupon(DiscountType.FIXED, BigDecimal.TEN, List.of());
            coupon.deactivate();

            CheckoutContext ctx = new CheckoutContext("client-1", new BigDecimal("100"), false, 0);
            ValidationResult result = coupon.validate(ctx);

            assertFalse(result.isValid());
            assertTrue(result.getErrors().get(0).contains("não está ativo"));
        }

        @Test
        @DisplayName("should pass with no rules and active coupon")
        void shouldPassWithNoRules() {
            Coupon coupon = createCoupon(DiscountType.FIXED, BigDecimal.TEN, List.of());
            CheckoutContext ctx = new CheckoutContext("client-1", new BigDecimal("100"), false, 0);

            assertTrue(coupon.validate(ctx).isValid());
        }

        @Test
        @DisplayName("should accumulate multiple rule violations")
        void shouldAccumulateViolations() {
            Coupon coupon = createCoupon(DiscountType.FIXED, BigDecimal.TEN, List.of(
                    new MinimumOrderValueRule(new BigDecimal("100")),
                    new ExpirationDateRule(LocalDateTime.now().minusDays(1)),
                    new SingleUsePerClientRule()
            ));

            CheckoutContext ctx = new CheckoutContext("client-1", new BigDecimal("50"), true, 0);
            ValidationResult result = coupon.validate(ctx);

            assertFalse(result.isValid());
            assertEquals(3, result.getErrors().size());
        }
    }

    @Nested
    @DisplayName("Lifecycle")
    class Lifecycle {

        @Test
        @DisplayName("deactivate should set active to false")
        void deactivateShouldSetInactive() {
            Coupon coupon = createCoupon(DiscountType.FIXED, BigDecimal.TEN, List.of());
            assertTrue(coupon.isActive());

            coupon.deactivate();
            assertFalse(coupon.isActive());
        }
    }
}
