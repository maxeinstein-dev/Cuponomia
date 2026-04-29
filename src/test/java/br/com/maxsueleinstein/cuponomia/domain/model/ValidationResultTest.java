package br.com.maxsueleinstein.cuponomia.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationResult Value Object")
class ValidationResultTest {

    @Test
    @DisplayName("ok() should be valid with no errors")
    void okShouldBeValid() {
        ValidationResult result = ValidationResult.ok();
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("fail() should be invalid with error message")
    void failShouldBeInvalid() {
        ValidationResult result = ValidationResult.fail("Some error");
        assertFalse(result.isValid());
        assertEquals(List.of("Some error"), result.getErrors());
    }

    @Test
    @DisplayName("combining two ok results should be ok")
    void combineTwoOkShouldBeOk() {
        ValidationResult combined = ValidationResult.ok().combine(ValidationResult.ok());
        assertTrue(combined.isValid());
    }

    @Test
    @DisplayName("combining ok with fail should be fail")
    void combineOkWithFailShouldBeFail() {
        ValidationResult combined = ValidationResult.ok().combine(ValidationResult.fail("Error"));
        assertFalse(combined.isValid());
        assertEquals(List.of("Error"), combined.getErrors());
    }

    @Test
    @DisplayName("combining two fails should accumulate errors")
    void combineTwoFailsShouldAccumulate() {
        ValidationResult combined = ValidationResult.fail("Error 1")
                .combine(ValidationResult.fail("Error 2"));
        assertFalse(combined.isValid());
        assertEquals(List.of("Error 1", "Error 2"), combined.getErrors());
    }

    @Test
    @DisplayName("errors list should be immutable")
    void errorsShouldBeImmutable() {
        ValidationResult result = ValidationResult.fail("Error");
        assertThrows(UnsupportedOperationException.class, () -> result.getErrors().add("Another"));
    }
}
