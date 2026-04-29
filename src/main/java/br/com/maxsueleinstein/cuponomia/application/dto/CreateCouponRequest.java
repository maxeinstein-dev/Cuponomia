package br.com.maxsueleinstein.cuponomia.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for creating a new coupon.
 */
@Schema(description = "Dados para criação de um novo cupom")
public record CreateCouponRequest(
                @NotBlank(message = "O código do cupom é obrigatório") @Schema(description = "Código único do cupom (3-30 caracteres alfanuméricos)", example = "MAX50") String code,

                @Schema(description = "Descrição do cupom", example = "🚀 Use o cupom MAX50 e tenha 50% de desconto na sua contratação.") String description,

                @NotNull(message = "O tipo de desconto é obrigatório (FIXED ou PERCENTAGE)") @Schema(description = "Tipo de desconto: FIXED (valor fixo) ou PERCENTAGE (percentual)", example = "PERCENTAGE") String discountType,

                @NotNull(message = "O valor do desconto é obrigatório") @Positive(message = "O valor do desconto deve ser positivo") @Schema(description = "Valor do desconto (R$ para FIXED, % para PERCENTAGE)", example = "50") BigDecimal discountValue,

                @Schema(description = "Regras de validação do cupom (todas opcionais)") RulesRequest rules) {
        /**
         * Nested DTO for coupon rules. All fields are optional.
         */
        @Schema(description = "Regras de validação do cupom")
        public record RulesRequest(
                        @Schema(description = "Valor mínimo do pedido para usar o cupom", example = "16000.00") BigDecimal minimumOrderValue,

                        @Schema(description = "Data/hora de expiração do cupom", example = "2027-12-31T23:59:59") LocalDateTime expiresAt,

                        @Schema(description = "Se true, cada cliente pode usar o cupom apenas uma vez", example = "true") Boolean singleUsePerClient,

                        @Schema(description = "Número máximo de usos totais do cupom", example = "1") Integer maxUsages) {
        }
}
