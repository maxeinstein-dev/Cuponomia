package br.com.maxsueleinstein.cuponomia.interfaces.rest;

import br.com.maxsueleinstein.cuponomia.application.dto.ApplyCouponResponse;
import br.com.maxsueleinstein.cuponomia.application.usecase.ApplyCouponUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "resilience4j.timelimiter.instances.checkout-timeout.timeout-duration=100ms",
        "resilience4j.timelimiter.instances.checkout-timeout.cancel-running-future=true"
})
@ActiveProfiles("test")
@DisplayName("Checkout timeout")
class CheckoutTimeoutIntegrationTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private ApplyCouponUseCase applyCouponUseCase;

    @Test
    @DisplayName("deve retornar gateway timeout quando o processamento exceder o limite")
    void shouldReturnGatewayTimeoutWhenProcessingExceedsLimit() {
        when(applyCouponUseCase.execute(any())).thenAnswer(invocation -> {
            Thread.sleep(300);
            return ApplyCouponResponse.success(
                    "SAVE20",
                    new BigDecimal("150.00"),
                    new BigDecimal("20.00"),
                    new BigDecimal("130.00"));
        });

        HttpServerErrorException exception = assertThrows(HttpServerErrorException.class,
                () -> RestClient.builder()
                        .baseUrl("http://localhost:" + port)
                        .build()
                        .post()
                        .uri("/api/v1/checkout/apply-coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("""
                                {
                                  "couponCode": "SAVE20",
                                  "clientId": "client-1",
                                  "orderTotal": 150.00
                                }
                                """)
                        .retrieve()
                        .body(ApplyCouponResponse.class));

        assertEquals(HttpStatus.GATEWAY_TIMEOUT, exception.getStatusCode());
    }
}