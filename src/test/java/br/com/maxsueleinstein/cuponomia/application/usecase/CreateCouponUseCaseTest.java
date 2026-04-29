package br.com.maxsueleinstein.cuponomia.application.usecase;

import br.com.maxsueleinstein.cuponomia.application.dto.CreateCouponRequest;
import br.com.maxsueleinstein.cuponomia.application.dto.CouponResponse;
import br.com.maxsueleinstein.cuponomia.application.mapper.CouponMapper;
import br.com.maxsueleinstein.cuponomia.domain.exception.DuplicateCouponCodeException;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.repository.CouponRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCouponUseCase")
class CreateCouponUseCaseTest {

    @Mock
    private CouponRepository couponRepository;

    private CouponMapper couponMapper;
    private CreateCouponUseCase useCase;

    @BeforeEach
    void setUp() {
        couponMapper = new CouponMapper();
        useCase = new CreateCouponUseCase(couponRepository, couponMapper);
    }

    @Test
    @DisplayName("deve criar cupom de desconto fixo com sucesso")
    void shouldCreateFixedCoupon() {
        when(couponRepository.existsByCode("SAVE20")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateCouponRequest request = new CreateCouponRequest(
                "SAVE20", "Economize R$ 20", "FIXED", new BigDecimal("20.00"),
                new CreateCouponRequest.RulesRequest(new BigDecimal("100.00"), null, null, null));

        CouponResponse response = useCase.execute(request);

        assertNotNull(response);
        assertEquals("SAVE20", response.code());
        assertEquals("FIXED", response.discountType());
        assertEquals(0, new BigDecimal("20.00").compareTo(response.discountValue()));
        assertTrue(response.active());
        assertEquals(0, new BigDecimal("100.00").compareTo(response.rules().minimumOrderValue()));
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    @DisplayName("deve criar cupom de desconto percentual com todas as regras")
    void shouldCreatePercentageCouponWithAllRules() {
        when(couponRepository.existsByCode("PCT15")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime expiration = LocalDateTime.now().plusDays(30);
        CreateCouponRequest request = new CreateCouponRequest(
                "PCT15", "15% de desconto", "PERCENTAGE", new BigDecimal("15"),
                new CreateCouponRequest.RulesRequest(new BigDecimal("50.00"), expiration, true, 500));

        CouponResponse response = useCase.execute(request);

        assertEquals("PERCENTAGE", response.discountType());
        assertNotNull(response.rules().minimumOrderValue());
        assertNotNull(response.rules().expiresAt());
        assertTrue(response.rules().singleUsePerClient());
        assertEquals(500, response.rules().maxUsages());
    }

    @Test
    @DisplayName("deve lançar DuplicateCouponCodeException para código já existente")
    void shouldThrowForDuplicateCode() {
        when(couponRepository.existsByCode("EXISTING")).thenReturn(true);

        CreateCouponRequest request = new CreateCouponRequest(
                "EXISTING", "desc", "FIXED", BigDecimal.TEN, null);

        assertThrows(DuplicateCouponCodeException.class, () -> useCase.execute(request));
        verify(couponRepository, never()).save(any());
    }
}
