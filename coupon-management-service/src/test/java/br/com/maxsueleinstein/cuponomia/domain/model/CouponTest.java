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

@DisplayName("Entidade Cupom")
class CouponTest {

    private Coupon createCoupon(DiscountType type, BigDecimal value, List<br.com.maxsueleinstein.cuponomia.domain.rule.CouponRule> rules) {
        return new Coupon(
                UUID.randomUUID(),
                new CouponCode("TEST10"),
                "Cupom de teste",
                type,
                value,
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                rules
        );
    }

    @Nested
    @DisplayName("Invariantes de construção")
    class ConstructionInvariants {

        @Test
        @DisplayName("deve rejeitar valor de desconto zero")
        void shouldRejectZeroDiscount() {
            assertThrows(IllegalArgumentException.class, () ->
                    createCoupon(DiscountType.FIXED, BigDecimal.ZERO, List.of()));
        }

        @Test
        @DisplayName("deve rejeitar valor de desconto negativo")
        void shouldRejectNegativeDiscount() {
            assertThrows(IllegalArgumentException.class, () ->
                    createCoupon(DiscountType.FIXED, new BigDecimal("-10"), List.of()));
        }

        @Test
        @DisplayName("deve rejeitar percentual acima de 100")
        void shouldRejectPercentageAbove100() {
            assertThrows(IllegalArgumentException.class, () ->
                    createCoupon(DiscountType.PERCENTAGE, new BigDecimal("101"), List.of()));
        }

        @Test
        @DisplayName("deve aceitar percentual exatamente igual a 100")
        void shouldAcceptPercentage100() {
            assertDoesNotThrow(() ->
                    createCoupon(DiscountType.PERCENTAGE, new BigDecimal("100"), List.of()));
        }

        @Test
        @DisplayName("deve rejeitar código nulo")
        void shouldRejectNullCode() {
            assertThrows(NullPointerException.class, () ->
                    new Coupon(UUID.randomUUID(), null, "desc", DiscountType.FIXED,
                            BigDecimal.TEN, true, null, null, null));
        }
    }

    @Nested
    @DisplayName("Cálculo de desconto")
    class DiscountCalculation {

        @Test
        @DisplayName("FIXO: deve retornar o valor do desconto")
        void fixedShouldReturnValue() {
            Coupon coupon = createCoupon(DiscountType.FIXED, new BigDecimal("20.00"), List.of());
            BigDecimal discount = coupon.applyDiscount(new BigDecimal("100.00"));
            assertEquals(0, new BigDecimal("20.00").compareTo(discount));
        }

        @Test
        @DisplayName("FIXO: deve limitar o desconto ao total do pedido")
        void fixedShouldCapAtOrderTotal() {
            Coupon coupon = createCoupon(DiscountType.FIXED, new BigDecimal("50.00"), List.of());
            BigDecimal discount = coupon.applyDiscount(new BigDecimal("30.00"));
            assertEquals(0, new BigDecimal("30.00").compareTo(discount));
        }

        @Test
        @DisplayName("PERCENTUAL: deve calcular corretamente")
        void percentageShouldCalculate() {
            Coupon coupon = createCoupon(DiscountType.PERCENTAGE, new BigDecimal("15"), List.of());
            BigDecimal discount = coupon.applyDiscount(new BigDecimal("200.00"));
            assertEquals(0, new BigDecimal("30.00").compareTo(discount));
        }

        @Test
        @DisplayName("PERCENTUAL 100%: deve retornar o total completo do pedido")
        void percentage100ShouldReturnFullTotal() {
            Coupon coupon = createCoupon(DiscountType.PERCENTAGE, new BigDecimal("100"), List.of());
            BigDecimal discount = coupon.applyDiscount(new BigDecimal("250.00"));
            assertEquals(0, new BigDecimal("250.00").compareTo(discount));
        }

        @Test
        @DisplayName("PERCENTUAL: deve arredondar para 2 casas decimais")
        void percentageShouldRound() {
            Coupon coupon = createCoupon(DiscountType.PERCENTAGE, new BigDecimal("33.33"), List.of());
            BigDecimal discount = coupon.applyDiscount(new BigDecimal("100.00"));
            assertEquals(2, discount.scale());
        }
    }

    @Nested
    @DisplayName("Validação")
    class Validation {

        @Test
        @DisplayName("deve falhar se o cupom estiver inativo")
        void shouldFailIfInactive() {
            Coupon coupon = createCoupon(DiscountType.FIXED, BigDecimal.TEN, List.of());
            coupon.deactivate();

            CheckoutContext ctx = new CheckoutContext("client-1", new BigDecimal("100"), false, 0);
            ValidationResult result = coupon.validate(ctx);

            assertFalse(result.isValid());
            assertTrue(result.getErrors().get(0).contains("não está ativo"));
        }

        @Test
        @DisplayName("deve passar sem regras com cupom ativo")
        void shouldPassWithNoRules() {
            Coupon coupon = createCoupon(DiscountType.FIXED, BigDecimal.TEN, List.of());
            CheckoutContext ctx = new CheckoutContext("client-1", new BigDecimal("100"), false, 0);

            assertTrue(coupon.validate(ctx).isValid());
        }

        @Test
        @DisplayName("deve acumular múltiplas violações de regras")
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
    @DisplayName("Ciclo de vida")
    class Lifecycle {

        @Test
        @DisplayName("desativar deve definir ativo como falso")
        void deactivateShouldSetInactive() {
            Coupon coupon = createCoupon(DiscountType.FIXED, BigDecimal.TEN, List.of());
            assertTrue(coupon.isActive());

            coupon.deactivate();
            assertFalse(coupon.isActive());
        }
    }
}
