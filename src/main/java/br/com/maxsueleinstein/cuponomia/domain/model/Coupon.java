package br.com.maxsueleinstein.cuponomia.domain.model;

import br.com.maxsueleinstein.cuponomia.domain.rule.CouponRule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade de domínio rica representando um cupom.
 * 
 * Encapsula lógica de negócio para:
 * - Cálculo de desconto (fixo ou percentual)
 * - Validação de regras via Specification Pattern
 * - Gerenciamento de ciclo de vida (ativação/desativação)
 * 
 * Invariantes garantidas:
 * - O valor do desconto deve ser positivo
 * - Desconto percentual deve estar entre 0 (exclusivo) e 100 (inclusivo)
 * - Código é imutável após a criação
 */
public class Coupon {

    private final UUID id;
    private final CouponCode code;
    private final String description;
    private final DiscountType discountType;
    private final BigDecimal discountValue;
    private boolean active;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private final List<CouponRule> rules;

    public Coupon(UUID id, CouponCode code, String description,
            DiscountType discountType, BigDecimal discountValue,
            boolean active, LocalDateTime createdAt, LocalDateTime updatedAt,
            List<CouponRule> rules) {
        Objects.requireNonNull(id, "O ID do cupom não pode ser nulo");
        Objects.requireNonNull(code, "O código do cupom não pode ser nulo");
        Objects.requireNonNull(discountType, "O tipo de desconto não pode ser nulo");
        Objects.requireNonNull(discountValue, "O valor do desconto não pode ser nulo");

        if (discountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do desconto deve ser positivo");
        }
        if (discountType == DiscountType.PERCENTAGE && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("O desconto percentual não pode exceder 100%");
        }

        this.id = id;
        this.code = code;
        this.description = description;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.active = active;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
        this.rules = rules != null ? new ArrayList<>(rules) : new ArrayList<>();
    }

    /**
     * Valida este cupom contra todas as suas regras usando o contexto de checkout.
     * Todas as regras são avaliadas, acumulando cada violação para um feedback
     * claro ao usuário.
     */
    public ValidationResult validate(CheckoutContext context) {
        if (!active) {
            return ValidationResult.fail("O cupom '" + code.getValue() + "' não está ativo");
        }

        ValidationResult result = ValidationResult.ok();
        for (CouponRule rule : rules) {
            result = result.combine(rule.validate(this, context));
        }
        return result;
    }

    /**
     * Calcula o valor do desconto para o total do pedido informado.
     * 
     * Para FIXED (Fixo): retorna o valor do desconto diretamente (limitado ao total
     * do pedido).
     * Para PERCENTAGE (Percentual): retorna (total do pedido * valor do desconto /
     * 100), arredondado para 2 casas decimais.
     * O resultado é sempre limitado ao total do pedido para evitar totais
     * negativos.
     */
    public BigDecimal applyDiscount(BigDecimal orderTotal) {
        Objects.requireNonNull(orderTotal, "O valor total do pedido não pode ser nulo");

        BigDecimal discount = switch (discountType) {
            case FIXED -> discountValue;
            case PERCENTAGE -> orderTotal
                    .multiply(discountValue)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        };

        // Limita o desconto ao total do pedido (nunca fica negativo)
        return discount.min(orderTotal);
    }

    /**
     * Desativa este cupom, impedindo seu uso futuro.
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void addRule(CouponRule rule) {
        Objects.requireNonNull(rule, "A regra não pode ser nula");
        this.rules.add(rule);
    }

    // Getters (sem setters — entidade rica com mutação controlada)

    public UUID getId() {
        return id;
    }

    public CouponCode getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public DiscountType getDiscountType() {
        return discountType;
    }

    public BigDecimal getDiscountValue() {
        return discountValue;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<CouponRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Coupon coupon = (Coupon) o;
        return Objects.equals(id, coupon.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Coupon{code=" + code + ", type=" + discountType + ", value=" + discountValue + ", active=" + active
                + "}";
    }
}
