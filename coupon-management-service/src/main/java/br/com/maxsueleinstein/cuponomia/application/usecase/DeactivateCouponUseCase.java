package br.com.maxsueleinstein.cuponomia.application.usecase;

import br.com.maxsueleinstein.cuponomia.application.dto.CouponResponse;
import br.com.maxsueleinstein.cuponomia.application.mapper.CouponMapper;
import br.com.maxsueleinstein.cuponomia.application.port.CouponEventPublisher;
import br.com.maxsueleinstein.cuponomia.domain.exception.CouponNotFoundException;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.model.CouponCode;
import br.com.maxsueleinstein.cuponomia.domain.repository.CouponRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de Uso: desativar um cupom por seu código.
 * Cupons desativados não podem ser usados no checkout.
 */
@Service
public class DeactivateCouponUseCase {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;
    private final CouponEventPublisher couponEventPublisher;

    public DeactivateCouponUseCase(CouponRepository couponRepository, CouponMapper couponMapper,
            CouponEventPublisher couponEventPublisher) {
        this.couponRepository = couponRepository;
        this.couponMapper = couponMapper;
        this.couponEventPublisher = couponEventPublisher;
    }

    @Transactional
    public CouponResponse execute(String code) {
        String normalizedCode = new CouponCode(code).getValue();
        Coupon coupon = couponRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new CouponNotFoundException(normalizedCode));

        coupon.deactivate();
        Coupon saved = couponRepository.save(coupon);
        couponEventPublisher.couponDeactivated(saved);

        return couponMapper.toResponse(saved);
    }
}
