package br.com.maxsueleinstein.cuponomia.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

/**
 * Response DTO for the result of applying a coupon to a checkout.
 * Provides clear feedback about success/failure and discount details.
 */
public record ApplyCouponResponse(
        boolean valid,
        String couponCode,
        BigDecimal originalTotal,
        BigDecimal discountApplied,
        BigDecimal finalTotal,
        String message,
        List<String> errors
) {
    public static ApplyCouponResponse success(String couponCode, BigDecimal originalTotal,
                                               BigDecimal discountApplied, BigDecimal finalTotal) {
        return new ApplyCouponResponse(
                true, couponCode, originalTotal, discountApplied, finalTotal,
                String.format(Locale.US, "Cupom aplicado com sucesso! Você economizou R$ %.2f", discountApplied),
                List.of()
        );
    }

    public static ApplyCouponResponse failure(String couponCode, BigDecimal originalTotal,
                                               List<String> errors) {
        return new ApplyCouponResponse(
                false, couponCode, originalTotal, BigDecimal.ZERO, originalTotal,
                "Falha na validação do cupom",
                errors
        );
    }
}
