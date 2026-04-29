package br.com.maxsueleinstein.cuponomia.domain.repository;

import br.com.maxsueleinstein.cuponomia.domain.model.CouponUsage;

import java.util.List;
import java.util.UUID;

/**
 * Port (interface) para persistência de registros de uso de cupons.
 * 
 * Suporta a consulta de uso para validação de uso único por cliente
 * e a contagem de usos totais para a regra de limite máximo de usos.
 */
public interface CouponUsageRepository {

    CouponUsage save(CouponUsage usage);

    boolean existsByCouponIdAndClientId(UUID couponId, String clientId);

    long countByCouponId(UUID couponId);

    List<CouponUsage> findByCouponId(UUID couponId);
}
