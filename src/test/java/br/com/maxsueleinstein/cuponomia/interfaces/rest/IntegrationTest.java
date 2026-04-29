package br.com.maxsueleinstein.cuponomia.interfaces.rest;

import br.com.maxsueleinstein.cuponomia.application.dto.ApplyCouponResponse;
import br.com.maxsueleinstein.cuponomia.application.dto.CouponResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the complete REST API.
 * <p>
 * Uses the full Spring Boot context with H2 in-memory database and random port.
 * Each test method gets a fresh database via @DirtiesContext.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Integration Tests")
class IntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    // === Helper methods ===

    private CouponResponse createCoupon(String json) {
        return restClient().post()
                .uri("/api/v1/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .body(CouponResponse.class);
    }

    private ApplyCouponResponse applyCoupon(String json) {
        return restClient().post()
                .uri("/api/v1/checkout/apply-coupon")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .body(ApplyCouponResponse.class);
    }

    private String fixedCouponJson() {
        return """
                {
                    "code": "SAVE20",
                    "description": "Save R$ 20",
                    "discountType": "FIXED",
                    "discountValue": 20.00,
                    "rules": {
                        "minimumOrderValue": 100.00,
                        "singleUsePerClient": true
                    }
                }
                """;
    }

    private String percentageCouponJson() {
        return """
                {
                    "code": "PCT15",
                    "description": "15%% off",
                    "discountType": "PERCENTAGE",
                    "discountValue": 15,
                    "rules": {
                        "minimumOrderValue": 50.00,
                        "expiresAt": "2030-12-31T23:59:59",
                        "singleUsePerClient": true,
                        "maxUsages": 1000
                    }
                }
                """;
    }

    @Nested
    @DisplayName("POST /api/v1/coupons")
    class CreateCouponIntegration {

        @Test
        @DisplayName("should create a fixed discount coupon")
        void shouldCreateFixedCoupon() {
            CouponResponse body = createCoupon(fixedCouponJson());

            assertNotNull(body);
            assertEquals("SAVE20", body.code());
            assertEquals("FIXED", body.discountType());
            assertEquals(0, body.discountValue().compareTo(new BigDecimal("20.00")));
            assertTrue(body.active());
            assertNotNull(body.id());
            assertEquals(0, body.rules().minimumOrderValue().compareTo(new BigDecimal("100.00")));
            assertTrue(body.rules().singleUsePerClient());
        }

        @Test
        @DisplayName("should return 409 for duplicate code")
        void shouldReturn409ForDuplicate() {
            createCoupon(fixedCouponJson());

            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                    () -> createCoupon(fixedCouponJson()));
            assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        }

        @Test
        @DisplayName("should return 400 for missing required fields")
        void shouldReturn400ForMissingFields() {
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> restClient().post()
                    .uri("/api/v1/coupons")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{}")
                    .retrieve()
                    .body(String.class));
            assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/coupons")
    class ListCouponsIntegration {

        @Test
        @DisplayName("should return empty list when no coupons exist")
        void shouldReturnEmptyList() {
            CouponResponse[] body = restClient().get()
                    .uri("/api/v1/coupons")
                    .retrieve()
                    .body(CouponResponse[].class);

            assertNotNull(body);
            assertEquals(0, body.length);
        }

        @Test
        @DisplayName("should list created coupons")
        void shouldListCoupons() {
            createCoupon(fixedCouponJson());
            createCoupon(percentageCouponJson());

            CouponResponse[] body = restClient().get()
                    .uri("/api/v1/coupons")
                    .retrieve()
                    .body(CouponResponse[].class);

            assertNotNull(body);
            assertEquals(2, body.length);
        }

        @Test
        @DisplayName("should filter by active status")
        void shouldFilterByActive() {
            createCoupon(fixedCouponJson());
            // Deactivate SAVE20
            restClient().patch()
                    .uri("/api/v1/coupons/SAVE20/deactivate")
                    .retrieve()
                    .body(CouponResponse.class);
            createCoupon(percentageCouponJson());

            CouponResponse[] body = restClient().get()
                    .uri("/api/v1/coupons?active=true")
                    .retrieve()
                    .body(CouponResponse[].class);

            assertNotNull(body);
            assertEquals(1, body.length);
            assertEquals("PCT15", body[0].code());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/coupons/{code}")
    class GetCouponIntegration {

        @Test
        @DisplayName("should return coupon by code")
        void shouldReturnByCode() {
            createCoupon(fixedCouponJson());

            CouponResponse body = restClient().get()
                    .uri("/api/v1/coupons/SAVE20")
                    .retrieve()
                    .body(CouponResponse.class);

            assertNotNull(body);
            assertEquals("SAVE20", body.code());
        }

        @Test
        @DisplayName("should return 404 for non-existent code")
        void shouldReturn404() {
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> restClient().get()
                    .uri("/api/v1/coupons/NOTFOUND")
                    .retrieve()
                    .body(String.class));
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/coupons/{code}/deactivate")
    class DeactivateCouponIntegration {

        @Test
        @DisplayName("should deactivate coupon")
        void shouldDeactivate() {
            createCoupon(fixedCouponJson());

            CouponResponse response = restClient().patch()
                    .uri("/api/v1/coupons/SAVE20/deactivate")
                    .retrieve()
                    .body(CouponResponse.class);

            assertNotNull(response);
            assertFalse(response.active());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/checkout/apply-coupon")
    class ApplyCouponIntegration {

        @Test
        @DisplayName("should apply coupon successfully")
        void shouldApplyCouponSuccessfully() {
            createCoupon(fixedCouponJson());

            ApplyCouponResponse body = applyCoupon("""
                    {
                        "couponCode": "SAVE20",
                        "clientId": "client-1",
                        "orderTotal": 150.00
                    }
                    """);

            assertNotNull(body);
            assertTrue(body.valid());
            assertEquals(0, body.discountApplied().compareTo(new BigDecimal("20.00")));
            assertEquals(0, body.finalTotal().compareTo(new BigDecimal("130.00")));
            assertTrue(body.message().contains("economizou"));
        }

        @Test
        @DisplayName("should fail when order below minimum")
        void shouldFailBelowMinimum() {
            createCoupon(fixedCouponJson());

            ApplyCouponResponse body = applyCoupon("""
                    {
                        "couponCode": "SAVE20",
                        "clientId": "client-1",
                        "orderTotal": 50.00
                    }
                    """);

            assertFalse(body.valid());
            assertEquals(0, BigDecimal.ZERO.compareTo(body.discountApplied()));
            assertFalse(body.errors().isEmpty());
        }

        @Test
        @DisplayName("should prevent reuse by same client (single use rule)")
        void shouldPreventReuse() {
            createCoupon(fixedCouponJson());

            // First use — should succeed
            ApplyCouponResponse first = applyCoupon("""
                    {
                        "couponCode": "SAVE20",
                        "clientId": "client-1",
                        "orderTotal": 150.00
                    }
                    """);
            assertTrue(first.valid());

            // Second use by same client — should fail
            ApplyCouponResponse second = applyCoupon("""
                    {
                        "couponCode": "SAVE20",
                        "clientId": "client-1",
                        "orderTotal": 150.00
                    }
                    """);
            assertFalse(second.valid());
            assertTrue(second.errors().get(0).contains("já utilizou"));
        }

        @Test
        @DisplayName("should allow different clients to use same coupon")
        void shouldAllowDifferentClients() {
            createCoupon(fixedCouponJson());

            ApplyCouponResponse first = applyCoupon("""
                    {
                        "couponCode": "SAVE20",
                        "clientId": "client-1",
                        "orderTotal": 150.00
                    }
                    """);
            assertTrue(first.valid());

            ApplyCouponResponse second = applyCoupon("""
                    {
                        "couponCode": "SAVE20",
                        "clientId": "client-2",
                        "orderTotal": 200.00
                    }
                    """);
            assertTrue(second.valid());
            assertEquals(0, second.discountApplied().compareTo(new BigDecimal("20.00")));
        }

        @Test
        @DisplayName("should return 404 for non-existent coupon")
        void shouldReturn404ForNonExistent() {
            HttpClientErrorException ex = assertThrows(HttpClientErrorException.class, () -> applyCoupon("""
                    {
                        "couponCode": "NOTREAL",
                        "clientId": "client-1",
                        "orderTotal": 100.00
                    }
                    """));
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        @DisplayName("should fail when coupon is deactivated")
        void shouldFailWhenDeactivated() {
            createCoupon(fixedCouponJson());
            restClient().patch()
                    .uri("/api/v1/coupons/SAVE20/deactivate")
                    .retrieve()
                    .body(CouponResponse.class);

            ApplyCouponResponse body = applyCoupon("""
                    {
                        "couponCode": "SAVE20",
                        "clientId": "client-1",
                        "orderTotal": 150.00
                    }
                    """);

            assertFalse(body.valid());
            assertTrue(body.errors().get(0).contains("não está ativo"));
        }

        @Test
        @DisplayName("should apply percentage discount correctly")
        void shouldApplyPercentageCorrectly() {
            createCoupon(percentageCouponJson());

            ApplyCouponResponse body = applyCoupon("""
                    {
                        "couponCode": "PCT15",
                        "clientId": "client-1",
                        "orderTotal": 200.00
                    }
                    """);

            assertTrue(body.valid());
            assertEquals(0, body.discountApplied().compareTo(new BigDecimal("30.00")));
            assertEquals(0, body.finalTotal().compareTo(new BigDecimal("170.00")));
        }

        @Test
        @DisplayName("should cap fixed discount at order total")
        void shouldCapFixedDiscountAtTotal() {
            createCoupon("""
                    {
                        "code": "BIG50",
                        "description": "Big discount",
                        "discountType": "FIXED",
                        "discountValue": 50.00,
                        "rules": {}
                    }
                    """);

            ApplyCouponResponse body = applyCoupon("""
                    {
                        "couponCode": "BIG50",
                        "clientId": "client-1",
                        "orderTotal": 30.00
                    }
                    """);

            assertTrue(body.valid());
            assertEquals(0, body.discountApplied().compareTo(new BigDecimal("30.00")));
            assertEquals(0, body.finalTotal().compareTo(BigDecimal.ZERO));
        }
    }
}
