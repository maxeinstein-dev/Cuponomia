package br.com.maxsueleinstein.cuponomia.domain.model;

/**
 * Representa o tipo de desconto que um cupom oferece.
 * 
 * FIXED: Um valor monetário fixo é subtraído do total do pedido (ex: R$ 20,00
 * de desconto).
 * PERCENTAGE: Um percentual do total do pedido é subtraído (ex: 15% de
 * desconto).
 */
public enum DiscountType {
    FIXED,
    PERCENTAGE
}
