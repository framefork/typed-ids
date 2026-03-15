package org.framefork.typedIds.spring.convert;

import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NumberToObjectBigIntIdConverterFactoryTest {

    private NumberToObjectBigIntIdConverterFactory factory;

    @BeforeEach
    void setUp() {
        factory = new NumberToObjectBigIntIdConverterFactory();
    }

    @Test
    void shouldConvertLongToObjectBigIntId() {
        // Given
        Converter<Number, TestBigIntId> converter = factory.getConverter(TestBigIntId.class);
        long value = 12345L;

        // When
        TestBigIntId result = converter.convert(value);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.toLong()).isEqualTo(value);
    }

    @Test
    void shouldConvertIntegerToObjectBigIntId() {
        // Given
        Converter<Number, TestBigIntId> converter = factory.getConverter(TestBigIntId.class);
        int value = 42;

        // When
        TestBigIntId result = converter.convert(value);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.toLong()).isEqualTo(value);
    }

    @Test
    void shouldReturnNullForNullInput() {
        // Given
        Converter<Number, TestBigIntId> converter = factory.getConverter(TestBigIntId.class);

        // When
        TestBigIntId result = converter.convert(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldThrowExceptionForInvalidType() {
        // Given
        Converter<Number, InvalidBigIntId> converter = factory.getConverter(InvalidBigIntId.class);

        // When / Then
        assertThatThrownBy(() -> converter.convert(123L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot convert Number to");
    }

    // Test ID class
    public static final class TestBigIntId extends ObjectBigIntId<TestBigIntId> {
        private TestBigIntId(long inner) {
            super(inner);
        }

        public static TestBigIntId from(long value) {
            return ObjectBigIntId.fromLong(TestBigIntId::new, value);
        }
    }

    // Invalid test class (no proper constructor)
    public static final class InvalidBigIntId extends ObjectBigIntId<InvalidBigIntId> {
        // This class intentionally has no long constructor to test error handling
        @SuppressWarnings({"UnusedMethod", "UnusedVariable"})
        private InvalidBigIntId(String invalid) {
            super(0);
        }
    }
}