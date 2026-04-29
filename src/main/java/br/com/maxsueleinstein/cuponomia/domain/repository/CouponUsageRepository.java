package br.com.maxsueleinstein.cuponomia.domain.repository;

import br.com.maxsueleinstein.cuponomia.domain.model.CouponUsage;

import java.util.List;
import java.util.UUID;

/**
 * Port (interface) for coupon usage persistence.
 * <p>
 * Supports querying usage for single-use validation
 * and counting total usages for max-usage rules.
 */
public interface CouponUsageRepository {

    CouponUsage save(CouponUsage usage);

    boolean existsByCouponIdAndClientId(UUID couponId, String clientId);

    long countByCouponId(UUID couponId);

    List<CouponUsage> findByCouponId(UUID couponId);
}
