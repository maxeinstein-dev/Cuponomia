package br.com.maxsueleinstein.cuponomia.domain.repository;

import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) for coupon persistence.
 * <p>
 * Defined in the domain layer — implementations live in infrastructure.
 * This inversion of dependency is the core of Hexagonal Architecture.
 */
public interface CouponRepository {

    Coupon save(Coupon coupon);

    Optional<Coupon> findByCode(String code);

    Optional<Coupon> findById(UUID id);

    List<Coupon> findAll();

    List<Coupon> findByActive(boolean active);

    boolean existsByCode(String code);
}
