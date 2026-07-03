package br.com.maxsueleinstein.cuponomia.support;

import br.com.maxsueleinstein.cuponomia.application.port.CouponEventPublisher;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class NoopCouponEventPublisher implements CouponEventPublisher {

    @Override
    public void couponCreated(Coupon coupon) {
    }

    @Override
    public void couponDeactivated(Coupon coupon) {
    }
}
