package br.com.maxsueleinstein.cuponomia.interfaces.rest;

import br.com.maxsueleinstein.cuponomia.application.dto.CouponResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("Coupon Management API")
class IntegrationTest {

    @LocalServerPort
    private int port;

    private RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @DisplayName("deve criar, listar, buscar e desativar cupons")
    void shouldManageCoupons() {
        CouponResponse created = createCoupon(fixedCouponJson());

        assertNotNull(created.id());
        assertEquals("SAVE20", created.code());
        assertEquals("FIXED", created.discountType());
        assertEquals(0, new BigDecimal("20.00").compareTo(created.discountValue()));

        CouponResponse[] coupons = restClient().get()
                .uri("/api/v1/coupons")
                .retrieve()
                .body(CouponResponse[].class);
        assertNotNull(coupons);
        assertEquals(1, coupons.length);

        CouponResponse found = restClient().get()
                .uri("/api/v1/coupons/SAVE20")
                .retrieve()
                .body(CouponResponse.class);
        assertNotNull(found);
        assertEquals("SAVE20", found.code());

        CouponResponse deactivated = restClient().patch()
                .uri("/api/v1/coupons/SAVE20/deactivate")
                .retrieve()
                .body(CouponResponse.class);
        assertNotNull(deactivated);
        assertFalse(deactivated.active());
    }

    @Test
    @DisplayName("deve retornar 409 para codigo duplicado")
    void shouldReturn409ForDuplicate() {
        createCoupon(fixedCouponJson());

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                () -> createCoupon(fixedCouponJson()));
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    private CouponResponse createCoupon(String json) {
        return restClient().post()
                .uri("/api/v1/coupons")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve()
                .body(CouponResponse.class);
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
}
