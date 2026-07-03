package br.com.maxsueleinstein.cuponomia.interfaces.rest;

import br.com.maxsueleinstein.cuponomia.application.dto.ApplyCouponResponse;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.model.CouponCode;
import br.com.maxsueleinstein.cuponomia.domain.model.DiscountType;
import br.com.maxsueleinstein.cuponomia.domain.repository.CouponRepository;
import br.com.maxsueleinstein.cuponomia.domain.rule.MinimumOrderValueRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.SingleUsePerClientRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Coupon Validation API")
class IntegrationTest {

    @LocalServerPort
    private int port;

    private final CouponRepository couponRepository;

    @Autowired
    IntegrationTest(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Test
    @DisplayName("deve validar e registrar uso usando a projecao local de cupons")
    void shouldApplyCouponFromLocalProjection() {
        couponRepository.save(new Coupon(
                UUID.randomUUID(),
                new CouponCode("SAVE20"),
                "Save R$ 20",
                DiscountType.FIXED,
                new BigDecimal("20.00"),
                true,
                LocalDateTime.now(),
                LocalDateTime.now(),
                List.of(new MinimumOrderValueRule(new BigDecimal("100.00")), new SingleUsePerClientRule())));

        ApplyCouponResponse first = applyCoupon("""
                {
                    "couponCode": "SAVE20",
                    "clientId": "client-1",
                    "orderTotal": 150.00
                }
                """);

        assertTrue(first.valid());
        assertEquals(0, new BigDecimal("20.00").compareTo(first.discountApplied()));
        assertEquals(0, new BigDecimal("130.00").compareTo(first.finalTotal()));

        ApplyCouponResponse second = applyCoupon("""
                {
                    "couponCode": "SAVE20",
                    "clientId": "client-1",
                    "orderTotal": 150.00
                }
                """);

        assertFalse(second.valid());
        assertFalse(second.errors().isEmpty());
    }

    private ApplyCouponResponse applyCoupon(String json) {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build()
                .post()
                .uri("/api/v1/checkout/apply-coupon")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .body(ApplyCouponResponse.class);
    }
}
