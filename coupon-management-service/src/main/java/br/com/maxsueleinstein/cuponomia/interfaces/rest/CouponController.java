package br.com.maxsueleinstein.cuponomia.interfaces.rest;

import br.com.maxsueleinstein.cuponomia.application.dto.CouponResponse;
import br.com.maxsueleinstein.cuponomia.application.dto.CreateCouponRequest;
import br.com.maxsueleinstein.cuponomia.application.usecase.CreateCouponUseCase;
import br.com.maxsueleinstein.cuponomia.application.usecase.DeactivateCouponUseCase;
import br.com.maxsueleinstein.cuponomia.application.usecase.GetCouponUseCase;
import br.com.maxsueleinstein.cuponomia.application.usecase.ListCouponsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
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
 * 
 * Controllers are thin — they only handle HTTP concerns (parsing, validation,
 * status codes) and delegate all business logic to use cases.
 */
@RestController
@RequestMapping("/api/v1/coupons")
@Tag(name = "Coupon Management", description = "Create, list, inspect, and deactivate discount coupons.")
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
        @Operation(summary = "Create a coupon", description = "Creates a coupon with a fixed or percentage discount and optional validation rules.")
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
                        description = "Coupon payload used by the demo guide. Change the code if you run the request more than once.",
                        required = true,
                        content = @Content(examples = @ExampleObject(name = "Create recruiter demo coupon", value = """
                                        {
                                          "code": "RECRUITER50",
                                          "description": "50% discount for a recruiter demo checkout.",
                                          "discountType": "PERCENTAGE",
                                          "discountValue": 50,
                                          "rules": {
                                            "minimumOrderValue": 16000.00,
                                            "expiresAt": "2027-12-31T23:59:59",
                                            "singleUsePerClient": true,
                                            "maxUsages": 10
                                          }
                                        }
                                        """)))
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "Coupon created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid request payload"),
                        @ApiResponse(responseCode = "409", description = "A coupon with this code already exists")
        })
        public ResponseEntity<CouponResponse> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
                CouponResponse response = createCouponUseCase.execute(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @GetMapping
        @Operation(summary = "List coupons", description = "Returns all coupons, optionally filtered by active/inactive status.")
        @ApiResponse(responseCode = "200", description = "Coupons returned successfully")
        public ResponseEntity<List<CouponResponse>> listCoupons(
                        @Parameter(description = "Filter by active status.", example = "true") @RequestParam(required = false) Boolean active) {
                return ResponseEntity.ok(listCouponsUseCase.execute(active));
        }

        @GetMapping("/{code}")
        @Operation(summary = "Get coupon by code", description = "Returns the public data for a coupon code.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Coupon found"),
                        @ApiResponse(responseCode = "404", description = "Coupon not found")
        })
        public ResponseEntity<CouponResponse> getCoupon(
                        @Parameter(description = "Coupon code.", example = "MAX50") @PathVariable String code) {
                return ResponseEntity.ok(getCouponUseCase.execute(code));
        }

        @PatchMapping("/{code}/deactivate")
        @Operation(summary = "Deactivate a coupon", description = "Marks a coupon as inactive so it can no longer be used in new checkouts.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Coupon deactivated successfully"),
                        @ApiResponse(responseCode = "404", description = "Coupon not found")
        })
        public ResponseEntity<CouponResponse> deactivateCoupon(
                        @Parameter(description = "Coupon code to deactivate.", example = "RECRUITER50") @PathVariable String code) {
                return ResponseEntity.ok(deactivateCouponUseCase.execute(code));
        }
}
