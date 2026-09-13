package br.com.maxsueleinstein.cuponomia.infrastructure.messaging;

import br.com.maxsueleinstein.cuponomia.application.port.CouponEventPublisher;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@ConditionalOnProperty(name = "cuponomia.kafka.enabled", havingValue = "false")
public class NoopCouponEventPublisher implements CouponEventPublisher {

    @Override
    public void couponCreated(Coupon coupon) {
    }

    @Override
    public void couponDeactivated(Coupon coupon) {
    }
}
