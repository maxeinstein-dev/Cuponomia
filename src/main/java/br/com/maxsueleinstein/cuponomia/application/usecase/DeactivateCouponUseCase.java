package br.com.maxsueleinstein.cuponomia.application.usecase;

import br.com.maxsueleinstein.cuponomia.application.dto.CouponResponse;
import br.com.maxsueleinstein.cuponomia.application.mapper.CouponMapper;
import br.com.maxsueleinstein.cuponomia.domain.exception.CouponNotFoundException;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.model.CouponCode;
import br.com.maxsueleinstein.cuponomia.domain.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: deactivate a coupon by its code.
 * Deactivated coupons cannot be used in checkout.
 */
@Service
public class DeactivateCouponUseCase {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    public DeactivateCouponUseCase(CouponRepository couponRepository, CouponMapper couponMapper) {
        this.couponRepository = couponRepository;
        this.couponMapper = couponMapper;
    }

    @Transactional
    public CouponResponse execute(String code) {
        String normalizedCode = new CouponCode(code).getValue();
        Coupon coupon = couponRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new CouponNotFoundException(normalizedCode));

        coupon.deactivate();
        Coupon saved = couponRepository.save(coupon);

        return couponMapper.toResponse(saved);
    }
}
