package org.framefork.typedIds.uuid;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class ObjectUuidTest
{

    @Nested
    final class Random
    {

        @Test
        public void generateRandom()
        {
            var id = ObjectUuidMock.random();
            Assertions.assertNotNull(id);
            assertThat(id.toNativeUuid().version()).isEqualTo(7);
        }

    }

    @Nested
    final class Equals
    {

        @ParameterizedTest
        @MethodSource("data")
        public void testOk(
            final boolean expected,
            final String left,
            final String right
        )
        {
            var leftId = ObjectUuidMock.from(left);
            var rightId = ObjectUuidMock.from(right);

            Assertions.assertEquals(expected, leftId.equals(rightId));
        }

        public static Stream<Arguments> data()
        {
            return Stream.of(
                Arguments.arguments(true, "33A7641C-811E-40B7-986E-AD109CFCF220", "33A7641C-811E-40B7-986E-AD109CFCF220"),
                Arguments.arguments(true, "33A7641C-811E-40B7-986E-AD109CFCF220", "33a7641c-811e-40b7-986e-ad109cfcf220"),
                Arguments.arguments(true, "33a7641c-811e-40b7-986e-ad109cfcf220", "33a7641c-811e-40b7-986e-ad109cfcf220"),
                Arguments.arguments(false, "9f6d74e3-961f-474e-8713-add459ad11e5", "33a7641c-811e-40b7-986e-ad109cfcf220"),
                Arguments.arguments(false, "33a7641c-811e-40b7-986e-ad109cfcf220", "9f6d74e3-961f-474e-8713-add459ad11e5")
            );
        }

    }

    @Nested
    final class ToString
    {

        public static Stream<Arguments> data()
        {
            return Stream.of(
                Arguments.arguments("33a7641c-811e-40b7-986e-ad109cfcf220", ObjectUuidMock.from("33A7641C-811E-40B7-986E-AD109CFCF220")),
                Arguments.arguments("33a7641c-811e-40b7-986e-ad109cfcf220", ObjectUuidMock.from("33a7641c-811e-40b7-986e-ad109cfcf220"))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        public void testOk(
            final String expected,
            final ObjectUuidMock uuidMock
        )
        {
            String actual = uuidMock.toString();
            Assertions.assertEquals(expected, actual);
        }

    }

    @Nested
    final class ConstructorValidation
    {

        @Test
        public void shouldAcceptUuidVersion4()
        {
            var uuid4 = UUID.randomUUID();
            Assertions.assertEquals(4, uuid4.version());

            var result = ObjectUuidMock.from(uuid4);

            Assertions.assertNotNull(result);
            Assertions.assertEquals(uuid4, result.toNativeUuid());
        }

        @Test
        public void shouldAcceptUuidVersion7()
        {
            var randomUuid = ObjectUuidMock.random();
            UUID uuid7 = randomUuid.toNativeUuid();
            Assertions.assertEquals(7, uuid7.version());

            var result = ObjectUuidMock.from(uuid7);

            Assertions.assertNotNull(result);
            Assertions.assertEquals(uuid7, result.toNativeUuid());
        }

        @Test
        public void shouldRejectUuidVersion1()
        {
            var uuid1 = new UUID(0x1000000000001000L, 0x8000000000000000L);
            Assertions.assertEquals(1, uuid1.version());

            IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> ObjectUuidMock.from(uuid1)
            );

            assertThat(exception).hasMessageContaining("Only versions [4, 7] are supported, bud version 1 was given");
        }

        @Test
        public void shouldRejectUuidVersion3()
        {
            var uuid3 = new UUID(0x1000000000003000L, 0x8000000000000000L);
            Assertions.assertEquals(3, uuid3.version());

            IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> ObjectUuidMock.from(uuid3)
            );

            assertThat(exception).hasMessageContaining("Only versions [4, 7] are supported, bud version 3 was given");
        }

        @Test
        public void shouldRejectUuidVersion5()
        {
            var uuid5 = new UUID(0x1000000000005000L, 0x8000000000000000L);
            Assertions.assertEquals(5, uuid5.version());

            IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> ObjectUuidMock.from(uuid5)
            );

            assertThat(exception).hasMessageContaining("Only versions [4, 7] are supported, bud version 5 was given");
        }

        @Test
        @SuppressWarnings("NullAway")
        public void shouldRejectNullUuid()
        {
            NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> ObjectUuidMock.from((UUID) null)
            );

            Assertions.assertEquals("given UUID must not be null", exception.getMessage());
        }

    }

    @Nested
    final class UuidConstruction
    {

        @Test
        public void shouldCreateFromUuid()
        {
            var uuid = UUID.fromString("33a7641c-811e-40b7-986e-ad109cfcf220");

            var result = ObjectUuidMock.from(uuid);

            Assertions.assertNotNull(result);
            Assertions.assertEquals(uuid, result.toNativeUuid());
        }

    }

    @Nested
    final class UuidConversion
    {

        @Test
        public void shouldConvertToNativeUuid()
        {
            String uuidString = "33a7641c-811e-40b7-986e-ad109cfcf220";

            var id = ObjectUuidMock.from(uuidString);

            UUID result = id.toNativeUuid();

            Assertions.assertEquals(UUID.fromString(uuidString), result);
        }

    }

    @Nested
    final class StringParsing
    {

        @Test
        public void shouldParseValidUuidString()
        {
            String uuidString = "33a7641c-811e-40b7-986e-ad109cfcf220";

            var result = ObjectUuidMock.from(uuidString);

            Assertions.assertNotNull(result);
            Assertions.assertEquals(uuidString, result.toString());
        }

        @Test
        public void shouldThrowOnInvalidUuidString()
        {
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> ObjectUuidMock.from("invalid-uuid-string")
            );
        }

        @Test
        @SuppressWarnings("NullAway")
        public void shouldThrowOnNullString()
        {
            Assertions.assertThrows(
                NullPointerException.class,
                () -> ObjectUuidMock.from((String) null)
            );
        }

    }

    @Nested
    final class CompareTo
    {

        @Test
        public void shouldCompareCorrectly()
        {
            var uuid1 = ObjectUuidMock.from("00000000-0000-4000-8000-000000000001");
            var uuid2 = ObjectUuidMock.from("00000000-0000-4000-8000-000000000002");

            int result = uuid1.compareTo(uuid2);

            assertThat(result).isNegative();
        }

        @Test
        public void shouldReturnZeroForEqualUuids()
        {
            var uuid1 = ObjectUuidMock.from("33a7641c-811e-40b7-986e-ad109cfcf220");
            var uuid2 = ObjectUuidMock.from("33a7641c-811e-40b7-986e-ad109cfcf220");

            int result = uuid1.compareTo(uuid2);

            Assertions.assertEquals(0, result);
        }

    }

    @Nested
    final class HashCode
    {

        @Test
        public void shouldReturnSameHashCodeForEqualUuids()
        {
            var uuid1 = ObjectUuidMock.from("33a7641c-811e-40b7-986e-ad109cfcf220");
            var uuid2 = ObjectUuidMock.from("33a7641c-811e-40b7-986e-ad109cfcf220");

            Assertions.assertEquals(uuid1.hashCode(), uuid2.hashCode());
        }

        @Test
        public void shouldReturnDifferentHashCodeForDifferentUuids()
        {
            var uuid1 = ObjectUuidMock.from("33a7641c-811e-40b7-986e-ad109cfcf220");
            var uuid2 = ObjectUuidMock.from("9f6d74e3-961f-474e-8713-add459ad11e5");

            Assertions.assertNotEquals(uuid1.hashCode(), uuid2.hashCode());
        }

    }

    @Nested
    final class EqualsEdgeCases
    {

        @Test
        public void shouldNotEqualNull()
        {
            var uuid = ObjectUuidMock.from("33a7641c-811e-40b7-986e-ad109cfcf220");

            assertThat(uuid).isNotEqualTo(null);
        }

        @Test
        public void shouldNotEqualDifferentType()
        {
            var uuid = ObjectUuidMock.from("33a7641c-811e-40b7-986e-ad109cfcf220");

            assertThat(uuid).isNotEqualTo("string");
        }

        @Test
        public void shouldNotEqualDifferentSubtype()
        {
            var uuid1 = ObjectUuidMock.from("33a7641c-811e-40b7-986e-ad109cfcf220");
            var uuid2 = ObjectUuidMock2.from("33a7641c-811e-40b7-986e-ad109cfcf220");

            assertThat(uuid1).isNotEqualTo(uuid2);
            assertThat(uuid2).isNotEqualTo(uuid1);
        }

        @Test
        public void shouldNotEqualNonObjectUuid()
        {
            var uuid = ObjectUuidMock.from("33a7641c-811e-40b7-986e-ad109cfcf220");

            assertThat(uuid).isNotEqualTo(UUID.fromString("33a7641c-811e-40b7-986e-ad109cfcf220"));
        }

    }

    @Nested
    final class Serialization
    {

        @Test
        public void shouldThrowOnJavaSerialization() throws Exception
        {
            var uuid = ObjectUuidMock.from("33a7641c-811e-40b7-986e-ad109cfcf220");
            try (var oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
                UnsupportedOperationException exception = Assertions.assertThrows(
                    UnsupportedOperationException.class,
                    () -> oos.writeObject(uuid)
                );

                assertThat(exception).hasMessage("Java Serialization is not supported. Do you have properly configured mapping for this type?");
            }
        }

    }

    public static class ObjectUuidMock2 extends ObjectUuid<ObjectUuidMock2>
    {

        private ObjectUuidMock2(final UUID inner)
        {
            super(inner);
        }

        public static ObjectUuidMock2 from(final String value)
        {
            return ObjectUuid.fromString(ObjectUuidMock2::new, value);
        }

    }

}
