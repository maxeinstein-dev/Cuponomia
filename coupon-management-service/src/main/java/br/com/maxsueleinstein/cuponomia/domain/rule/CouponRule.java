package br.com.maxsueleinstein.cuponomia.domain.rule;

import br.com.maxsueleinstein.cuponomia.domain.model.CheckoutContext;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.model.ValidationResult;

/**
 * Interface do Specification Pattern para regras de validação de cupons.
 * 
 * Cada implementação encapsula uma única preocupação de validação.
 * As regras são componíveis — um cupom pode ter múltiplas regras que são
 * todas avaliadas para fornecer um feedback abrangente.
 */
public interface CouponRule {

    /**
     * Valida o cupom contra esta regra no contexto de checkout fornecido.
     *
     * @param coupon  o cupom sendo validado
     * @param context o contexto de checkout com informações do pedido e do cliente
     * @return ValidationResult.ok() se a regra passar, ou ValidationResult.fail()
     *         com uma mensagem de erro
     */
    ValidationResult validate(Coupon coupon, CheckoutContext context);

    /**
     * Retorna uma descrição legível por humanos desta regra.
     */
    String description();
}
