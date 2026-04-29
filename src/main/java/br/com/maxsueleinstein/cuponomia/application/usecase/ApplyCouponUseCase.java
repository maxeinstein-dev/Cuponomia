package br.com.maxsueleinstein.cuponomia.application.usecase;

import br.com.maxsueleinstein.cuponomia.application.dto.ApplyCouponRequest;
import br.com.maxsueleinstein.cuponomia.application.dto.ApplyCouponResponse;
import br.com.maxsueleinstein.cuponomia.domain.exception.CouponNotFoundException;
import br.com.maxsueleinstein.cuponomia.domain.model.CheckoutContext;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.model.CouponUsage;
import br.com.maxsueleinstein.cuponomia.domain.model.ValidationResult;
import br.com.maxsueleinstein.cuponomia.domain.repository.CouponRepository;
import br.com.maxsueleinstein.cuponomia.domain.repository.CouponUsageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Use case: apply a coupon to a checkout.
 * <p>
 * Orchestrates:
 * 1. Coupon lookup
 * 2. Context enrichment (loads usage data for rule validation)
 * 3. Rule validation (all rules evaluated for comprehensive feedback)
 * 4. Discount calculation
 * 5. Usage registration
 * <p>
 * Uses @Transactional to ensure atomicity between validation and usage
 * recording,
 * preventing race conditions where two threads validate the same single-use
 * coupon.
 */
@Service
public class ApplyCouponUseCase {

        private final CouponRepository couponRepository;
        private final CouponUsageRepository couponUsageRepository;

        public ApplyCouponUseCase(CouponRepository couponRepository,
                        CouponUsageRepository couponUsageRepository) {
                this.couponRepository = couponRepository;
                this.couponUsageRepository = couponUsageRepository;
        }

        @Transactional
        public ApplyCouponResponse execute(ApplyCouponRequest request) {
                // 1. Lookup coupon by code
                String code = request.couponCode().trim().toUpperCase();
                Coupon coupon = couponRepository.findByCode(code)
                                .orElseThrow(() -> new CouponNotFoundException(code));

                // 2. Enrich checkout context with usage data
                boolean clientAlreadyUsed = couponUsageRepository
                                .existsByCouponIdAndClientId(coupon.getId(), request.clientId());
                long totalUsages = couponUsageRepository.countByCouponId(coupon.getId());

                CheckoutContext context = new CheckoutContext(
                                request.clientId(),
                                request.orderTotal(),
                                clientAlreadyUsed,
                                totalUsages);

                // 3. Validate all rules
                ValidationResult result = coupon.validate(context);

                if (!result.isValid()) {
                        return ApplyCouponResponse.failure(code, request.orderTotal(), result.getErrors());
                }

                // 4. Calculate discount
                BigDecimal discount = coupon.applyDiscount(request.orderTotal());
                BigDecimal finalTotal = request.orderTotal().subtract(discount);

                // 5. Register usage
                CouponUsage usage = CouponUsage.create(
                                coupon.getId(), request.clientId(),
                                request.orderTotal(), discount);
                couponUsageRepository.save(usage);

                return ApplyCouponResponse.success(code, request.orderTotal(), discount, finalTotal);
        }
}
