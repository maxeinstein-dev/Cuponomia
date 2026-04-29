package br.com.maxsueleinstein.cuponomia.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Value Object ValidationResult")
class ValidationResultTest {

    @Test
    @DisplayName("ok() deve ser válido sem erros")
    void okShouldBeValid() {
        ValidationResult result = ValidationResult.ok();
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("fail() deve ser inválido com mensagem de erro")
    void failShouldBeInvalid() {
        ValidationResult result = ValidationResult.fail("Algum erro");
        assertFalse(result.isValid());
        assertEquals(List.of("Algum erro"), result.getErrors());
    }

    @Test
    @DisplayName("combinar dois resultados ok deve resultar em ok")
    void combineTwoOkShouldBeOk() {
        ValidationResult combined = ValidationResult.ok().combine(ValidationResult.ok());
        assertTrue(combined.isValid());
    }

    @Test
    @DisplayName("combinar ok com falha deve resultar em falha")
    void combineOkWithFailShouldBeFail() {
        ValidationResult combined = ValidationResult.ok().combine(ValidationResult.fail("Erro"));
        assertFalse(combined.isValid());
        assertEquals(List.of("Erro"), combined.getErrors());
    }

    @Test
    @DisplayName("combinar duas falhas deve acumular os erros")
    void combineTwoFailsShouldAccumulate() {
        ValidationResult combined = ValidationResult.fail("Erro 1")
                .combine(ValidationResult.fail("Erro 2"));
        assertFalse(combined.isValid());
        assertEquals(List.of("Erro 1", "Erro 2"), combined.getErrors());
    }

    @Test
    @DisplayName("a lista de erros deve ser imutável")
    void errorsShouldBeImmutable() {
        ValidationResult result = ValidationResult.fail("Erro");
        assertThrows(UnsupportedOperationException.class, () -> result.getErrors().add("Outro"));
    }
}
