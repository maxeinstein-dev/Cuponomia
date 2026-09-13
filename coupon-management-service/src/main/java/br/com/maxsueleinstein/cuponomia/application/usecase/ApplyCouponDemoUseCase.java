package br.com.maxsueleinstein.cuponomia.application.usecase;

import br.com.maxsueleinstein.cuponomia.application.dto.ApplyCouponRequest;
import br.com.maxsueleinstein.cuponomia.application.dto.ApplyCouponResponse;
import br.com.maxsueleinstein.cuponomia.domain.exception.CouponNotFoundException;
import br.com.maxsueleinstein.cuponomia.domain.model.CheckoutContext;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.model.ValidationResult;
import br.com.maxsueleinstein.cuponomia.domain.repository.CouponRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ApplyCouponDemoUseCase {

    private final CouponRepository couponRepository;
    private final ConcurrentMap<String, Set<String>> couponUsagesByClient = new ConcurrentHashMap<>();

    public ApplyCouponDemoUseCase(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public ApplyCouponResponse execute(ApplyCouponRequest request) {
        Coupon coupon = couponRepository.findByCode(request.couponCode())
                .orElseThrow(() -> new CouponNotFoundException(request.couponCode()));

        Set<String> clients = couponUsagesByClient.computeIfAbsent(
                coupon.getCode().getValue(),
                ignored -> ConcurrentHashMap.newKeySet());
        CheckoutContext context = new CheckoutContext(
                request.clientId(),
                request.orderTotal(),
                clients.contains(request.clientId()),
                clients.size());

        ValidationResult result = coupon.validate(context);
        if (!result.isValid()) {
            return ApplyCouponResponse.failure(coupon.getCode().getValue(), request.orderTotal(), result.getErrors());
        }

        BigDecimal discount = coupon.applyDiscount(request.orderTotal());
        BigDecimal finalTotal = request.orderTotal().subtract(discount);
        clients.add(request.clientId());
        return ApplyCouponResponse.success(coupon.getCode().getValue(), request.orderTotal(), discount, finalTotal);
    }
}
