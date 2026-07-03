package br.com.maxsueleinstein.cuponomia.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Value Object CouponCode")
class CouponCodeTest {

    @Nested
    @DisplayName("Criação")
    class Creation {

        @Test
        @DisplayName("deve criar código a partir de string válida")
        void shouldCreateFromValidString() {
            CouponCode code = new CouponCode("SUMMER2026");
            assertEquals("SUMMER2026", code.getValue());
        }

        @Test
        @DisplayName("deve normalizar para maiúsculas")
        void shouldNormalizeToUppercase() {
            CouponCode code = new CouponCode("summer2026");
            assertEquals("SUMMER2026", code.getValue());
        }

        @Test
        @DisplayName("deve remover espaços em branco")
        void shouldTrimWhitespace() {
            CouponCode code = new CouponCode("  SAVE10  ");
            assertEquals("SAVE10", code.getValue());
        }

        @Test
        @DisplayName("deve aceitar hífens e underscores")
        void shouldAcceptHyphensAndUnderscores() {
            assertDoesNotThrow(() -> new CouponCode("SAVE-10_PCT"));
        }

        @Test
        @DisplayName("deve aceitar comprimento mínimo (3 caracteres)")
        void shouldAcceptMinimumLength() {
            assertDoesNotThrow(() -> new CouponCode("ABC"));
        }
    }

    @Nested
    @DisplayName("Falhas de validação")
    class ValidationFailures {

        @Test
        @DisplayName("deve rejeitar nulo")
        void shouldRejectNull() {
            assertThrows(IllegalArgumentException.class, () -> new CouponCode(null));
        }

        @Test
        @DisplayName("deve rejeitar string em branco")
        void shouldRejectBlank() {
            assertThrows(IllegalArgumentException.class, () -> new CouponCode("   "));
        }

        @Test
        @DisplayName("deve rejeitar código muito curto (2 caracteres)")
        void shouldRejectTooShort() {
            assertThrows(IllegalArgumentException.class, () -> new CouponCode("AB"));
        }

        @Test
        @DisplayName("deve rejeitar caracteres especiais")
        void shouldRejectSpecialChars() {
            assertThrows(IllegalArgumentException.class, () -> new CouponCode("SAVE@10!"));
        }
    }

    @Nested
    @DisplayName("Igualdade")
    class Equality {

        @Test
        @DisplayName("deve ser igual para o mesmo valor")
        void shouldBeEqualForSameValue() {
            CouponCode a = new CouponCode("SAVE10");
            CouponCode b = new CouponCode("save10");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("não deve ser igual para valores diferentes")
        void shouldNotBeEqualForDifferentValues() {
            CouponCode a = new CouponCode("SAVE10");
            CouponCode b = new CouponCode("SAVE20");
            assertNotEquals(a, b);
        }
    }
}
