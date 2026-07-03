package br.com.maxsueleinstein.cuponomia.infrastructure.messaging.kafka;

import br.com.maxsueleinstein.cuponomia.contracts.event.CouponChangedEvent;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.model.CouponCode;
import br.com.maxsueleinstein.cuponomia.domain.model.DiscountType;
import br.com.maxsueleinstein.cuponomia.domain.repository.CouponRepository;
import br.com.maxsueleinstein.cuponomia.domain.rule.CouponRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.ExpirationDateRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.MaxUsageRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.MinimumOrderValueRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.SingleUsePerClientRule;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("!test")
public class CouponProjectionListener {

    private final CouponRepository couponRepository;

    public CouponProjectionListener(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    @Transactional
    @KafkaListener(
            topics = "${cuponomia.kafka.topics.coupon-events}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void onCouponChanged(CouponChangedEvent event) {
        couponRepository.save(toDomain(event));
    }

    private Coupon toDomain(CouponChangedEvent event) {
        return new Coupon(
                event.couponId(),
                new CouponCode(event.code()),
                event.description(),
                DiscountType.valueOf(event.discountType()),
                event.discountValue(),
                event.active(),
                event.createdAt(),
                event.updatedAt(),
                toRules(event.rules()));
    }

    private List<CouponRule> toRules(CouponChangedEvent.RulesPayload rulesPayload) {
        List<CouponRule> rules = new ArrayList<>();
        if (rulesPayload == null) {
            return rules;
        }

        if (rulesPayload.minimumOrderValue() != null
                && rulesPayload.minimumOrderValue().compareTo(BigDecimal.ZERO) > 0) {
            rules.add(new MinimumOrderValueRule(rulesPayload.minimumOrderValue()));
        }
        if (rulesPayload.expiresAt() != null) {
            rules.add(new ExpirationDateRule(rulesPayload.expiresAt()));
        }
        if (rulesPayload.singleUsePerClient()) {
            rules.add(new SingleUsePerClientRule());
        }
        if (rulesPayload.maxUsages() != null && rulesPayload.maxUsages() > 0) {
            rules.add(new MaxUsageRule(rulesPayload.maxUsages()));
        }

        return rules;
    }
}
