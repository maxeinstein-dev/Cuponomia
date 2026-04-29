package br.com.maxsueleinstein.cuponomia.interfaces.rest;

import br.com.maxsueleinstein.cuponomia.application.dto.CouponResponse;
import br.com.maxsueleinstein.cuponomia.application.dto.CreateCouponRequest;
import br.com.maxsueleinstein.cuponomia.application.usecase.CreateCouponUseCase;
import br.com.maxsueleinstein.cuponomia.application.usecase.DeactivateCouponUseCase;
import br.com.maxsueleinstein.cuponomia.application.usecase.GetCouponUseCase;
import br.com.maxsueleinstein.cuponomia.application.usecase.ListCouponsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for coupon CRUD operations.
 * <p>
 * Controllers are thin — they only handle HTTP concerns (parsing, validation,
 * status codes) and delegate all business logic to use cases.
 */
@RestController
@RequestMapping("/api/v1/coupons")
@Tag(name = "Cupons", description = "Operações de gerenciamento de cupons de desconto")
public class CouponController {

    private final CreateCouponUseCase createCouponUseCase;
    private final GetCouponUseCase getCouponUseCase;
    private final ListCouponsUseCase listCouponsUseCase;
    private final DeactivateCouponUseCase deactivateCouponUseCase;

    public CouponController(CreateCouponUseCase createCouponUseCase,
                            GetCouponUseCase getCouponUseCase,
                            ListCouponsUseCase listCouponsUseCase,
                            DeactivateCouponUseCase deactivateCouponUseCase) {
        this.createCouponUseCase = createCouponUseCase;
        this.getCouponUseCase = getCouponUseCase;
        this.listCouponsUseCase = listCouponsUseCase;
        this.deactivateCouponUseCase = deactivateCouponUseCase;
    }

    @PostMapping
    @Operation(summary = "Criar novo cupom",
            description = "Cria um cupom com tipo de desconto (fixo ou percentual), valor e regras de validação opcionais")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cupom criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos na requisição"),
            @ApiResponse(responseCode = "409", description = "Já existe um cupom com este código")
    })
    public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        CouponResponse response = createCouponUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar todos os cupons",
            description = "Retorna todos os cupons cadastrados, com filtro opcional por status ativo/inativo")
    @ApiResponse(responseCode = "200", description = "Lista de cupons retornada com sucesso")
    public ResponseEntity<List<CouponResponse>> listCoupons(
            @Parameter(description = "Filtrar por status ativo (true/false)")
            @RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(listCouponsUseCase.execute(active));
    }

    @GetMapping("/{code}")
    @Operation(summary = "Buscar cupom por código",
            description = "Retorna os dados de um cupom a partir do seu código único")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cupom encontrado"),
            @ApiResponse(responseCode = "404", description = "Cupom não encontrado")
    })
    public ResponseEntity<CouponResponse> getCoupon(
            @Parameter(description = "Código do cupom", example = "VERAO15")
            @PathVariable String code) {
        return ResponseEntity.ok(getCouponUseCase.execute(code));
    }

    @PatchMapping("/{code}/deactivate")
    @Operation(summary = "Desativar um cupom",
            description = "Marca o cupom como inativo, impedindo seu uso em novos checkouts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cupom desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cupom não encontrado")
    })
    public ResponseEntity<CouponResponse> deactivateCoupon(
            @Parameter(description = "Código do cupom a desativar", example = "VERAO15")
            @PathVariable String code) {
        return ResponseEntity.ok(deactivateCouponUseCase.execute(code));
    }
}
