package org.framefork.typedIds.spring.convert;

import org.framefork.typedIds.uuid.ObjectUuid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.converter.Converter;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StringToObjectUuidConverterFactoryTest {

    private StringToObjectUuidConverterFactory factory;

    @BeforeEach
    void setUp() {
        factory = new StringToObjectUuidConverterFactory();
    }

    @Test
    void shouldConvertStringToObjectUuid() {
        // Given
        Converter<String, TestUuid> converter = factory.getConverter(TestUuid.class);
        UUID uuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        String uuidString = uuid.toString();

        // When
        TestUuid result = converter.convert(uuidString);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.toNativeUuid()).isEqualTo(uuid);
    }

    @Test
    void shouldReturnNullForNullInput() {
        // Given
        Converter<String, TestUuid> converter = factory.getConverter(TestUuid.class);

        // When
        TestUuid result = converter.convert(null);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldReturnNullForEmptyString() {
        // Given
        Converter<String, TestUuid> converter = factory.getConverter(TestUuid.class);

        // When
        TestUuid result = converter.convert("");

        // Then
        assertThat(result).isNull();
    }

    @Test
    void shouldThrowExceptionForInvalidUuidString() {
        // Given
        Converter<String, TestUuid> converter = factory.getConverter(TestUuid.class);

        // When / Then
        assertThatThrownBy(() -> converter.convert("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionForInvalidType() {
        // Given
        Converter<String, InvalidUuid> converter = factory.getConverter(InvalidUuid.class);

        // When / Then
        assertThatThrownBy(() -> converter.convert("550e8400-e29b-41d4-a716-446655440000"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot convert String to");
    }

    // Test UUID class - using UUID v4 for testing
    public static final class TestUuid extends ObjectUuid<TestUuid> {
        private TestUuid(UUID inner) {
            super(inner);
        }

        public static TestUuid from(UUID value) {
            return ObjectUuid.fromUuid(TestUuid::new, value);
        }

        public static TestUuid from(String value) {
            return ObjectUuid.fromString(TestUuid::new, value);
        }
    }

    // Invalid test class (no proper constructor)
    public static final class InvalidUuid extends ObjectUuid<InvalidUuid> {
        // This class intentionally has no UUID constructor to test error handling
        @SuppressWarnings({"UnusedMethod", "UnusedVariable"})
        private InvalidUuid(String invalid) {
            super(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        }
    }
}