package br.com.maxsueleinstein.cuponomia.application.usecase;

import br.com.maxsueleinstein.cuponomia.application.dto.CouponResponse;
import br.com.maxsueleinstein.cuponomia.application.mapper.CouponMapper;
import br.com.maxsueleinstein.cuponomia.domain.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use case: list all coupons, optionally filtered by active status.
 */
@Service
public class ListCouponsUseCase {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    public ListCouponsUseCase(CouponRepository couponRepository, CouponMapper couponMapper) {
        this.couponRepository = couponRepository;
        this.couponMapper = couponMapper;
    }

    @Transactional(readOnly = true)
    public List<CouponResponse> execute(Boolean active) {
        if (active != null) {
            return couponRepository.findByActive(active).stream()
                    .map(couponMapper::toResponse)
                    .toList();
        }
        return couponRepository.findAll().stream()
                .map(couponMapper::toResponse)
                .toList();
    }
}
