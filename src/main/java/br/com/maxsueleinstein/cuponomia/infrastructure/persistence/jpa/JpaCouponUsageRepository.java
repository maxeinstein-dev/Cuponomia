package br.com.maxsueleinstein.cuponomia.infrastructure.persistence.jpa;

import br.com.maxsueleinstein.cuponomia.infrastructure.persistence.entity.CouponUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for CouponUsageEntity.
 */
@Repository
public interface JpaCouponUsageRepository extends JpaRepository<CouponUsageEntity, UUID> {

    boolean existsByCouponIdAndClientId(UUID couponId, String clientId);

    long countByCouponId(UUID couponId);

    List<CouponUsageEntity> findByCouponId(UUID couponId);
}
