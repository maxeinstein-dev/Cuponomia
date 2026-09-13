package br.com.maxsueleinstein.cuponomia.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Checkout payload for applying a coupon in the single Swagger demo")
public record ApplyCouponRequest(
        @NotBlank(message = "O código do cupom é obrigatório")
        @Schema(description = "Coupon code to apply", example = "MAX50")
        String couponCode,

        @NotBlank(message = "O ID do cliente é obrigatório")
        @Schema(description = "Client identifier for usage rules", example = "recruiter-demo")
        String clientId,

        @NotNull(message = "O valor total do pedido é obrigatório")
        @Positive(message = "O valor total do pedido deve ser positivo")
        @Schema(description = "Order total before discount", example = "16000.00")
        BigDecimal orderTotal) {
}
