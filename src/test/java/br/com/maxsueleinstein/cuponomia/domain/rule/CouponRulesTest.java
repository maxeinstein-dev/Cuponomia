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

@DisplayName("Regras de Cupom (Specification Pattern)")
class CouponRulesTest {

    private final Coupon coupon = new Coupon(
            UUID.randomUUID(), new CouponCode("TEST"), "Teste",
            DiscountType.FIXED, BigDecimal.TEN, true,
            LocalDateTime.now(), LocalDateTime.now(), List.of()
    );

    @Nested
    @DisplayName("MinimumOrderValueRule")
    class MinimumOrderValueRuleTest {

        private final MinimumOrderValueRule rule = new MinimumOrderValueRule(new BigDecimal("100.00"));

        @Test
        @DisplayName("deve passar quando o pedido atinge o valor mínimo")
        void shouldPassWhenMeetsMinimum() {
            CheckoutContext ctx = new CheckoutContext("c1", new BigDecimal("100.00"), false, 0);
            assertTrue(rule.validate(coupon, ctx).isValid());
        }

        @Test
        @DisplayName("deve passar quando o pedido supera o valor mínimo")
        void shouldPassWhenExceedsMinimum() {
            CheckoutContext ctx = new CheckoutContext("c1", new BigDecimal("200.00"), false, 0);
            assertTrue(rule.validate(coupon, ctx).isValid());
        }

        @Test
        @DisplayName("deve falhar quando o pedido está abaixo do valor mínimo")
        void shouldFailWhenBelowMinimum() {
            CheckoutContext ctx = new CheckoutContext("c1", new BigDecimal("50.00"), false, 0);
            ValidationResult result = rule.validate(coupon, ctx);
            assertFalse(result.isValid());
            String error = result.getErrors().get(0).toLowerCase();
            assertTrue(error.contains("abaixo") || error.contains("mínimo"),
                    "Mensagem de erro deve mencionar 'abaixo' ou 'mínimo'. Recebido: " + error);
        }

        @Test
        @DisplayName("deve rejeitar valor mínimo não positivo")
        void shouldRejectNonPositiveMinimum() {
            assertThrows(IllegalArgumentException.class, () -> new MinimumOrderValueRule(BigDecimal.ZERO));
        }
    }

    @Nested
    @DisplayName("ExpirationDateRule")
    class ExpirationDateRuleTest {

        @Test
        @DisplayName("deve passar quando o cupom não está expirado")
        void shouldPassWhenNotExpired() {
            ExpirationDateRule rule = new ExpirationDateRule(LocalDateTime.now().plusDays(30));
            CheckoutContext ctx = new CheckoutContext("c1", BigDecimal.TEN, false, 0);
            assertTrue(rule.validate(coupon, ctx).isValid());
        }

        @Test
        @DisplayName("deve falhar quando o cupom está expirado")
        void shouldFailWhenExpired() {
            ExpirationDateRule rule = new ExpirationDateRule(LocalDateTime.now().minusDays(1));
            CheckoutContext ctx = new CheckoutContext("c1", BigDecimal.TEN, false, 0);
            ValidationResult result = rule.validate(coupon, ctx);
            assertFalse(result.isValid());
            assertTrue(result.getErrors().get(0).contains("expirou"));
        }

        @Test
        @DisplayName("deve rejeitar data de expiração nula")
        void shouldRejectNullDate() {
            assertThrows(NullPointerException.class, () -> new ExpirationDateRule(null));
        }
    }

    @Nested
    @DisplayName("SingleUsePerClientRule")
    class SingleUsePerClientRuleTest {

        private final SingleUsePerClientRule rule = new SingleUsePerClientRule();

        @Test
        @DisplayName("deve passar quando o cliente ainda não usou o cupom")
        void shouldPassWhenNotUsed() {
            CheckoutContext ctx = new CheckoutContext("c1", BigDecimal.TEN, false, 0);
            assertTrue(rule.validate(coupon, ctx).isValid());
        }

        @Test
        @DisplayName("deve falhar quando o cliente já usou o cupom")
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
        @DisplayName("deve passar quando o número de usos está abaixo do limite")
        void shouldPassWhenBelowMax() {
            CheckoutContext ctx = new CheckoutContext("c1", BigDecimal.TEN, false, 50);
            assertTrue(rule.validate(coupon, ctx).isValid());
        }

        @Test
        @DisplayName("deve falhar quando o número de usos atinge o limite máximo")
        void shouldFailWhenAtMax() {
            CheckoutContext ctx = new CheckoutContext("c1", BigDecimal.TEN, false, 100);
            ValidationResult result = rule.validate(coupon, ctx);
            assertFalse(result.isValid());
            assertTrue(result.getErrors().get(0).contains("limite máximo"));
        }

        @Test
        @DisplayName("deve falhar quando o número de usos supera o limite máximo")
        void shouldFailWhenAboveMax() {
            CheckoutContext ctx = new CheckoutContext("c1", BigDecimal.TEN, false, 101);
            assertFalse(rule.validate(coupon, ctx).isValid());
        }

        @Test
        @DisplayName("deve rejeitar limite máximo de usos não positivo")
        void shouldRejectNonPositiveMax() {
            assertThrows(IllegalArgumentException.class, () -> new MaxUsageRule(0));
            assertThrows(IllegalArgumentException.class, () -> new MaxUsageRule(-1));
        }
    }
}
