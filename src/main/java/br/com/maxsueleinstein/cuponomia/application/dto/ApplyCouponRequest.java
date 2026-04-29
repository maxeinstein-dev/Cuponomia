package br.com.maxsueleinstein.cuponomia.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * DTO for applying a coupon to a checkout.
 */
@Schema(description = "Dados para aplicar um cupom no checkout")
public record ApplyCouponRequest(
        @NotBlank(message = "O código do cupom é obrigatório")
        @Schema(description = "Código do cupom a ser aplicado", example = "MAX100")
        String couponCode,

        @NotBlank(message = "O ID do cliente é obrigatório")
        @Schema(description = "Identificador único do cliente", example = "rafa-caceres")
        String clientId,

        @NotNull(message = "O valor total do pedido é obrigatório")
        @Positive(message = "O valor total do pedido deve ser positivo")
        @Schema(description = "Valor total do pedido em reais", example = "8000.00")
        BigDecimal orderTotal
) {}
