package br.com.maxsueleinstein.cuponomia.infrastructure.persistence.jpa;

import br.com.maxsueleinstein.cuponomia.infrastructure.persistence.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for CouponEntity.
 */
@Repository
public interface JpaCouponRepository extends JpaRepository<CouponEntity, UUID> {

    Optional<CouponEntity> findByCode(String code);

    boolean existsByCode(String code);

    List<CouponEntity> findByActive(boolean active);
}
