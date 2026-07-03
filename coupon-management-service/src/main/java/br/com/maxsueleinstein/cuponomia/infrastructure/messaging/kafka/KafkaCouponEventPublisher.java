package br.com.maxsueleinstein.cuponomia.infrastructure.messaging.kafka;

import br.com.maxsueleinstein.cuponomia.application.port.CouponEventPublisher;
import br.com.maxsueleinstein.cuponomia.contracts.event.CouponChangedEvent;
import br.com.maxsueleinstein.cuponomia.contracts.event.CouponEventType;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.rule.CouponRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.ExpirationDateRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.MaxUsageRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.MinimumOrderValueRule;
import br.com.maxsueleinstein.cuponomia.domain.rule.SingleUsePerClientRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Profile("!test")
public class KafkaCouponEventPublisher implements CouponEventPublisher {

    private final KafkaTemplate<String, CouponChangedEvent> kafkaTemplate;
    private final String topic;

    public KafkaCouponEventPublisher(KafkaTemplate<String, CouponChangedEvent> kafkaTemplate,
            @Value("${cuponomia.kafka.topics.coupon-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void couponCreated(Coupon coupon) {
        publish(toEvent(CouponEventType.COUPON_CREATED, coupon));
    }

    @Override
    public void couponDeactivated(Coupon coupon) {
        publish(toEvent(CouponEventType.COUPON_DEACTIVATED, coupon));
    }

    private void publish(CouponChangedEvent event) {
        Runnable send = () -> kafkaTemplate.send(topic, event.code(), event);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send.run();
                }
            });
            return;
        }

        send.run();
    }

    private CouponChangedEvent toEvent(CouponEventType eventType, Coupon coupon) {
        return new CouponChangedEvent(
                UUID.randomUUID(),
                eventType,
                Instant.now(),
                coupon.getId(),
                coupon.getCode().getValue(),
                coupon.getDescription(),
                coupon.getDiscountType().name(),
                coupon.getDiscountValue(),
                coupon.isActive(),
                toRulesPayload(coupon),
                coupon.getCreatedAt(),
                coupon.getUpdatedAt());
    }

    private CouponChangedEvent.RulesPayload toRulesPayload(Coupon coupon) {
        BigDecimal minimumOrderValue = null;
        LocalDateTime expiresAt = null;
        boolean singleUsePerClient = false;
        Integer maxUsages = null;

        for (CouponRule rule : coupon.getRules()) {
            if (rule instanceof MinimumOrderValueRule minimumRule) {
                minimumOrderValue = minimumRule.getMinimumValue();
            } else if (rule instanceof ExpirationDateRule expirationRule) {
                expiresAt = expirationRule.getExpiresAt();
            } else if (rule instanceof SingleUsePerClientRule) {
                singleUsePerClient = true;
            } else if (rule instanceof MaxUsageRule maxUsageRule) {
                maxUsages = maxUsageRule.getMaxUses();
            }
        }

        return new CouponChangedEvent.RulesPayload(minimumOrderValue, expiresAt, singleUsePerClient, maxUsages);
    }
}
