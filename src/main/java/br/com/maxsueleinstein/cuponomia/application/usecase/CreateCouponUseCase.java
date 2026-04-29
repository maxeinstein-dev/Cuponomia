package br.com.maxsueleinstein.cuponomia.application.usecase;

import br.com.maxsueleinstein.cuponomia.application.dto.CouponResponse;
import br.com.maxsueleinstein.cuponomia.application.dto.CreateCouponRequest;
import br.com.maxsueleinstein.cuponomia.application.mapper.CouponMapper;
import br.com.maxsueleinstein.cuponomia.domain.exception.DuplicateCouponCodeException;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.model.CouponCode;
import br.com.maxsueleinstein.cuponomia.domain.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: create a new coupon with its discount type and validation rules.
 */
@Service
public class CreateCouponUseCase {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    public CreateCouponUseCase(CouponRepository couponRepository, CouponMapper couponMapper) {
        this.couponRepository = couponRepository;
        this.couponMapper = couponMapper;
    }

    @Transactional
    public CouponResponse execute(CreateCouponRequest request) {
        // Normalize and validate code early for a clear error message
        String normalizedCode = new CouponCode(request.code()).getValue();

        if (couponRepository.existsByCode(normalizedCode)) {
            throw new DuplicateCouponCodeException(normalizedCode);
        }

        Coupon coupon = couponMapper.toDomain(request);
        Coupon saved = couponRepository.save(coupon);

        return couponMapper.toResponse(saved);
    }
}
