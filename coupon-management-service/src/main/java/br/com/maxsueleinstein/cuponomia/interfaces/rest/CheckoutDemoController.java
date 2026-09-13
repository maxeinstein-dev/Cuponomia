package br.com.maxsueleinstein.cuponomia.interfaces.rest;

import br.com.maxsueleinstein.cuponomia.application.dto.ApplyCouponRequest;
import br.com.maxsueleinstein.cuponomia.application.dto.ApplyCouponResponse;
import br.com.maxsueleinstein.cuponomia.application.usecase.ApplyCouponDemoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/checkout")
@Tag(name = "Checkout Validation", description = "Apply coupons during checkout in the single-service Render demo.")
public class CheckoutDemoController {

    private final ApplyCouponDemoUseCase applyCouponDemoUseCase;

    public CheckoutDemoController(ApplyCouponDemoUseCase applyCouponDemoUseCase) {
        this.applyCouponDemoUseCase = applyCouponDemoUseCase;
    }

    @PostMapping("/apply-coupon")
    @Operation(
            summary = "Apply coupon at checkout",
            description = "Demo endpoint exposed in the management service so the free Render deployment can show coupon creation and checkout validation in one Swagger.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Create MAX50 first if the database has just been provisioned.",
            required = true,
            content = @Content(examples = @ExampleObject(name = "Apply MAX50 coupon", value = """
                    {
                      "couponCode": "MAX50",
                      "clientId": "recruiter-demo",
                      "orderTotal": 16000.00
                    }
                    """)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Coupon processed. Check the valid field for the business result."),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    public ResponseEntity<ApplyCouponResponse> applyCoupon(@Valid @RequestBody ApplyCouponRequest request) {
        return ResponseEntity.ok(applyCouponDemoUseCase.execute(request));
    }
}
