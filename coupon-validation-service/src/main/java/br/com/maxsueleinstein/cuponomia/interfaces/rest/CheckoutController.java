package br.com.maxsueleinstein.cuponomia.interfaces.rest;

import br.com.maxsueleinstein.cuponomia.application.dto.ApplyCouponRequest;
import br.com.maxsueleinstein.cuponomia.application.dto.ApplyCouponResponse;
import br.com.maxsueleinstein.cuponomia.application.usecase.CheckoutTimeoutService;
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

import java.util.concurrent.CompletionStage;

/**
 * REST controller for checkout operations.
 * 
 * Handles coupon application during checkout with comprehensive validation
 * feedback.
 */
@RestController
@RequestMapping("/api/v1/checkout")
@Tag(name = "Checkout Validation", description = "Apply coupons during checkout and inspect validation results.")
public class CheckoutController {

    private final CheckoutTimeoutService checkoutTimeoutService;

    public CheckoutController(CheckoutTimeoutService checkoutTimeoutService) {
        this.checkoutTimeoutService = checkoutTimeoutService;
    }

    @PostMapping("/apply-coupon")
    @Operation(summary = "Apply coupon at checkout", description = "Validates and applies a coupon to the submitted order total.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Checkout payload used by the demo guide. MAX50 is included in the seed data.",
            required = true,
            content = @Content(examples = @ExampleObject(name = "Apply MAX50 coupon", value = """
                    {
                      "couponCode": "MAX50",
                      "clientId": "recruiter-demo",
                      "orderTotal": 16000.00
                    }
                    """)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Coupon processed. Check the 'valid' field for the business result."),
            @ApiResponse(responseCode = "400", description = "Invalid request payload"),
            @ApiResponse(responseCode = "404", description = "Coupon not found")
    })
    public CompletionStage<ResponseEntity<ApplyCouponResponse>> applyCoupon(
            @Valid @RequestBody ApplyCouponRequest request) {
        return checkoutTimeoutService.execute(request).thenApply(ResponseEntity::ok);
    }
}
