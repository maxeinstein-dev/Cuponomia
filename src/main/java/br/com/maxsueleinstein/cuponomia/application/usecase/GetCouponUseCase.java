package br.com.maxsueleinstein.cuponomia.application.usecase;

import br.com.maxsueleinstein.cuponomia.application.dto.CouponResponse;
import br.com.maxsueleinstein.cuponomia.application.mapper.CouponMapper;
import br.com.maxsueleinstein.cuponomia.domain.exception.CouponNotFoundException;
import br.com.maxsueleinstein.cuponomia.domain.model.CouponCode;
import br.com.maxsueleinstein.cuponomia.domain.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: retrieve a coupon by its code.
 */
@Service
public class GetCouponUseCase {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    public GetCouponUseCase(CouponRepository couponRepository, CouponMapper couponMapper) {
        this.couponRepository = couponRepository;
        this.couponMapper = couponMapper;
    }

    @Transactional(readOnly = true)
    public CouponResponse execute(String code) {
        String normalizedCode = new CouponCode(code).getValue();
        return couponRepository.findByCode(normalizedCode)
                .map(couponMapper::toResponse)
                .orElseThrow(() -> new CouponNotFoundException(normalizedCode));
    }
}
