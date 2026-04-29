package br.com.maxsueleinstein.cuponomia.interfaces.rest;

import br.com.maxsueleinstein.cuponomia.application.dto.ApplyCouponRequest;
import br.com.maxsueleinstein.cuponomia.application.dto.ApplyCouponResponse;
import br.com.maxsueleinstein.cuponomia.application.usecase.ApplyCouponUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for checkout operations.
 * <p>
 * Handles coupon application during checkout with comprehensive validation feedback.
 */
@RestController
@RequestMapping("/api/v1/checkout")
@Tag(name = "Checkout", description = "Operações de aplicação de cupom no checkout")
public class CheckoutController {

    private final ApplyCouponUseCase applyCouponUseCase;

    public CheckoutController(ApplyCouponUseCase applyCouponUseCase) {
        this.applyCouponUseCase = applyCouponUseCase;
    }

    @PostMapping("/apply-coupon")
    @Operation(summary = "Aplicar cupom no checkout",
            description = "Valida e aplica um cupom ao pedido informado. Retorna os detalhes do desconto ou os erros de validação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cupom processado (verifique o campo 'valid' para o resultado)"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição"),
            @ApiResponse(responseCode = "404", description = "Cupom não encontrado")
    })
    public ResponseEntity<ApplyCouponResponse> applyCoupon(@Valid @RequestBody ApplyCouponRequest request) {
        ApplyCouponResponse response = applyCouponUseCase.execute(request);
        return ResponseEntity.ok(response);
    }
}
