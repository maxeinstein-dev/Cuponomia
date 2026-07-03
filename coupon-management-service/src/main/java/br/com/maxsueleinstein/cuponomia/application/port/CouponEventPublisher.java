package br.com.maxsueleinstein.cuponomia.application.port;

import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;

public interface CouponEventPublisher {

    void couponCreated(Coupon coupon);

    void couponDeactivated(Coupon coupon);
}
