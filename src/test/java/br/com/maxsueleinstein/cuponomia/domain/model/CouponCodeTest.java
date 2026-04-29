package br.com.maxsueleinstein.cuponomia.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CouponCode Value Object")
class CouponCodeTest {

    @Nested
    @DisplayName("Creation")
    class Creation {

        @Test
        @DisplayName("should create code from valid string")
        void shouldCreateFromValidString() {
            CouponCode code = new CouponCode("SUMMER2026");
            assertEquals("SUMMER2026", code.getValue());
        }

        @Test
        @DisplayName("should normalize to uppercase")
        void shouldNormalizeToUppercase() {
            CouponCode code = new CouponCode("summer2026");
            assertEquals("SUMMER2026", code.getValue());
        }

        @Test
        @DisplayName("should trim whitespace")
        void shouldTrimWhitespace() {
            CouponCode code = new CouponCode("  SAVE10  ");
            assertEquals("SAVE10", code.getValue());
        }

        @Test
        @DisplayName("should accept hyphens and underscores")
        void shouldAcceptHyphensAndUnderscores() {
            assertDoesNotThrow(() -> new CouponCode("SAVE-10_PCT"));
        }

        @Test
        @DisplayName("should accept minimum length (3 chars)")
        void shouldAcceptMinimumLength() {
            assertDoesNotThrow(() -> new CouponCode("ABC"));
        }
    }

    @Nested
    @DisplayName("Validation failures")
    class ValidationFailures {

        @Test
        @DisplayName("should reject null")
        void shouldRejectNull() {
            assertThrows(IllegalArgumentException.class, () -> new CouponCode(null));
        }

        @Test
        @DisplayName("should reject blank string")
        void shouldRejectBlank() {
            assertThrows(IllegalArgumentException.class, () -> new CouponCode("   "));
        }

        @Test
        @DisplayName("should reject too short (2 chars)")
        void shouldRejectTooShort() {
            assertThrows(IllegalArgumentException.class, () -> new CouponCode("AB"));
        }

        @Test
        @DisplayName("should reject special characters")
        void shouldRejectSpecialChars() {
            assertThrows(IllegalArgumentException.class, () -> new CouponCode("SAVE@10!"));
        }
    }

    @Nested
    @DisplayName("Equality")
    class Equality {

        @Test
        @DisplayName("should be equal for same value")
        void shouldBeEqualForSameValue() {
            CouponCode a = new CouponCode("SAVE10");
            CouponCode b = new CouponCode("save10");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("should not be equal for different values")
        void shouldNotBeEqualForDifferentValues() {
            CouponCode a = new CouponCode("SAVE10");
            CouponCode b = new CouponCode("SAVE20");
            assertNotEquals(a, b);
        }
    }
}
