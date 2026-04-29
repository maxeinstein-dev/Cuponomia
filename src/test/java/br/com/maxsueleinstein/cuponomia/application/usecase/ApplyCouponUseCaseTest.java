package br.com.maxsueleinstein.cuponomia.application.usecase;

import br.com.maxsueleinstein.cuponomia.application.dto.ApplyCouponRequest;
import br.com.maxsueleinstein.cuponomia.application.dto.ApplyCouponResponse;
import br.com.maxsueleinstein.cuponomia.domain.exception.CouponNotFoundException;
import br.com.maxsueleinstein.cuponomia.domain.model.*;
import br.com.maxsueleinstein.cuponomia.domain.repository.CouponRepository;
import br.com.maxsueleinstein.cuponomia.domain.repository.CouponUsageRepository;
import br.com.maxsueleinstein.cuponomia.domain.rule.MinimumOrderValueRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.SingleUsePerClientRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplyCouponUseCase")
class ApplyCouponUseCaseTest {

    @Mock
    private CouponRepository couponRepository;
    @Mock
    private CouponUsageRepository couponUsageRepository;

    private ApplyCouponUseCase useCase;

    private final UUID couponId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ApplyCouponUseCase(couponRepository, couponUsageRepository);
    }

    private Coupon createTestCoupon(DiscountType type, BigDecimal value, List<br.com.maxsueleinstein.cuponomia.domain.rule.CouponRule> rules) {
        return new Coupon(couponId, new CouponCode("SAVE10"), "Test", type, value,
                true, LocalDateTime.now(), LocalDateTime.now(), rules);
    }

    @Nested
    @DisplayName("Successful application")
    class SuccessfulApplication {

        @Test
        @DisplayName("should apply fixed discount and register usage")
        void shouldApplyFixedDiscount() {
            Coupon coupon = createTestCoupon(DiscountType.FIXED, new BigDecimal("20.00"), List.of());
            when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(coupon));
            when(couponUsageRepository.existsByCouponIdAndClientId(couponId, "client-1")).thenReturn(false);
            when(couponUsageRepository.countByCouponId(couponId)).thenReturn(0L);
            when(couponUsageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ApplyCouponRequest request = new ApplyCouponRequest("SAVE10", "client-1", new BigDecimal("100.00"));
            ApplyCouponResponse response = useCase.execute(request);

            assertTrue(response.valid());
            assertEquals(0, new BigDecimal("20.00").compareTo(response.discountApplied()));
            assertEquals(0, new BigDecimal("80.00").compareTo(response.finalTotal()));
            verify(couponUsageRepository).save(any(CouponUsage.class));
        }

        @Test
        @DisplayName("should apply percentage discount correctly")
        void shouldApplyPercentageDiscount() {
            Coupon coupon = createTestCoupon(DiscountType.PERCENTAGE, new BigDecimal("15"), List.of());
            when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(coupon));
            when(couponUsageRepository.existsByCouponIdAndClientId(any(), any())).thenReturn(false);
            when(couponUsageRepository.countByCouponId(any())).thenReturn(0L);
            when(couponUsageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ApplyCouponRequest request = new ApplyCouponRequest("save10", "client-1", new BigDecimal("200.00"));
            ApplyCouponResponse response = useCase.execute(request);

            assertTrue(response.valid());
            assertEquals(0, new BigDecimal("30.00").compareTo(response.discountApplied()));
            assertEquals(0, new BigDecimal("170.00").compareTo(response.finalTotal()));
        }
    }

    @Nested
    @DisplayName("Validation failures")
    class ValidationFailures {

        @Test
        @DisplayName("should return failure when coupon not found")
        void shouldThrowWhenNotFound() {
            when(couponRepository.findByCode("INVALID")).thenReturn(Optional.empty());

            ApplyCouponRequest request = new ApplyCouponRequest("INVALID", "client-1", new BigDecimal("100.00"));
            assertThrows(CouponNotFoundException.class, () -> useCase.execute(request));
            verify(couponUsageRepository, never()).save(any());
        }

        @Test
        @DisplayName("should return failure when order below minimum")
        void shouldFailWhenBelowMinimum() {
            Coupon coupon = createTestCoupon(DiscountType.FIXED, BigDecimal.TEN,
                    List.of(new MinimumOrderValueRule(new BigDecimal("100"))));
            when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(coupon));
            when(couponUsageRepository.existsByCouponIdAndClientId(any(), any())).thenReturn(false);
            when(couponUsageRepository.countByCouponId(any())).thenReturn(0L);

            ApplyCouponRequest request = new ApplyCouponRequest("SAVE10", "client-1", new BigDecimal("50.00"));
            ApplyCouponResponse response = useCase.execute(request);

            assertFalse(response.valid());
            assertEquals(0, BigDecimal.ZERO.compareTo(response.discountApplied()));
            assertEquals(0, new BigDecimal("50.00").compareTo(response.finalTotal()));
            assertFalse(response.errors().isEmpty());
            verify(couponUsageRepository, never()).save(any());
        }

        @Test
        @DisplayName("should return failure when client already used coupon")
        void shouldFailWhenAlreadyUsed() {
            Coupon coupon = createTestCoupon(DiscountType.FIXED, BigDecimal.TEN,
                    List.of(new SingleUsePerClientRule()));
            when(couponRepository.findByCode("SAVE10")).thenReturn(Optional.of(coupon));
            when(couponUsageRepository.existsByCouponIdAndClientId(couponId, "client-1")).thenReturn(true);
            when(couponUsageRepository.countByCouponId(couponId)).thenReturn(1L);

            ApplyCouponRequest request = new ApplyCouponRequest("SAVE10", "client-1", new BigDecimal("100.00"));
            ApplyCouponResponse response = useCase.execute(request);

            assertFalse(response.valid());
            assertTrue(response.errors().get(0).contains("já utilizou"));
            verify(couponUsageRepository, never()).save(any());
        }
    }
}
