package br.com.maxsueleinstein.cuponomia.domain.repository;

import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port (interface) para persistência de cupons.
 * 
 * Definida na camada de domínio — as implementações residem na infraestrutura.
 * Essa inversão de dependência é o núcleo da Arquitetura Hexagonal.
 */
public interface CouponRepository {

    Coupon save(Coupon coupon);

    Optional<Coupon> findByCode(String code);

    Optional<Coupon> findById(UUID id);

    List<Coupon> findAll();

    List<Coupon> findByActive(boolean active);

    boolean existsByCode(String code);
}
