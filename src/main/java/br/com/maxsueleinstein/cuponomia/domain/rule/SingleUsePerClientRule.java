package br.com.maxsueleinstein.cuponomia.domain.rule;

import br.com.maxsueleinstein.cuponomia.domain.model.CheckoutContext;
import br.com.maxsueleinstein.cuponomia.domain.model.Coupon;
import br.com.maxsueleinstein.cuponomia.domain.model.ValidationResult;

/**
 * Regra: cada cliente só pode usar este cupom uma vez.
 * 
 * O CheckoutContext carrega uma flag pré-carregada indicando se
 * o cliente já utilizou este cupom. Isso mantém a camada de domínio
 * pura — sem a necessidade de acessar o repositório aqui.
 */
public class SingleUsePerClientRule implements CouponRule {

    @Override
    public ValidationResult validate(Coupon coupon, CheckoutContext context) {
        if (context.hasClientAlreadyUsedCoupon()) {
            return ValidationResult.fail(
                    String.format("O cliente '%s' já utilizou o cupom '%s'",
                            context.getClientId(), coupon.getCode().getValue()));
        }
        return ValidationResult.ok();
    }

    @Override
    public String description() {
        return "Uso único por cliente";
    }
}
