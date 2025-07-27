package org.framefork.typedIds.bigint;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class ObjectBigIntIdTest
{

    @Nested
    final class Random
    {

        @Test
        public void generateRandom()
        {
            ObjectBigIntIdMock id = ObjectBigIntIdMock.random();
            Assertions.assertNotNull(id);
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
            ObjectBigIntIdMock leftId = ObjectBigIntIdMock.from(left);
            ObjectBigIntIdMock rightId = ObjectBigIntIdMock.from(right);

            Assertions.assertEquals(expected, leftId.equals(rightId));
        }

        public static Stream<Arguments> data()
        {
            return Stream.of(
                Arguments.arguments(true, "693968158089838711", "693968158089838711"),
                Arguments.arguments(false, "693968158089838711", "693968158089738711"),
                Arguments.arguments(false, "693968158089738711", "693968158089838711")
            );
        }

    }

    @Nested
    final class ToString
    {

        public static Stream<Arguments> data()
        {
            return Stream.of(
                Arguments.arguments("693968158089838711", ObjectBigIntIdMock.from("693968158089838711"))
            );
        }

        @ParameterizedTest
        @MethodSource("data")
        public void testOk(
            final String expected,
            final ObjectBigIntIdMock idMock
        )
        {
            String actual = idMock.toString();
            Assertions.assertEquals(expected, actual);
        }

    }

    @Nested
    final class ConstructorValidation
    {

        @Test
        public void shouldAcceptValidLong()
        {
            long value = 123456789L;

            var result = ObjectBigIntIdMock.from(value);

            Assertions.assertNotNull(result);
            Assertions.assertEquals(value, result.toLong());
        }

    }

    @Nested
    final class StringParsing
    {

        @Test
        public void shouldParseValidLongString()
        {
            String longString = "693968158089838711";

            var result = ObjectBigIntIdMock.from(longString);

            Assertions.assertNotNull(result);
            Assertions.assertEquals(longString, result.toString());
        }

        @Test
        public void shouldParseMaxLong()
        {
            String maxLongString = String.valueOf(Long.MAX_VALUE);

            var result = ObjectBigIntIdMock.from(maxLongString);

            Assertions.assertNotNull(result);
            Assertions.assertEquals(Long.MAX_VALUE, result.toLong());
        }

        @Test
        public void shouldParseMinLong()
        {
            String minLongString = String.valueOf(Long.MIN_VALUE);

            var result = ObjectBigIntIdMock.from(minLongString);

            Assertions.assertNotNull(result);
            Assertions.assertEquals(Long.MIN_VALUE, result.toLong());
        }

        @Test
        public void shouldThrowOnInvalidString()
        {
            NumberFormatException exception = Assertions.assertThrows(
                NumberFormatException.class,
                () -> ObjectBigIntIdMock.from("not-a-number")
            );

            assertThat(exception).hasMessageContaining("not-a-number");
        }

        @Test
        public void shouldThrowOnEmptyString()
        {
            NumberFormatException exception = Assertions.assertThrows(
                NumberFormatException.class,
                () -> ObjectBigIntIdMock.from("")
            );

            assertThat(exception).hasMessage("For input string: \"\"");
        }

        @Test
        @SuppressWarnings("NullAway")
        public void shouldThrowOnNullString()
        {
            NumberFormatException exception = Assertions.assertThrows(
                NumberFormatException.class,
                () -> ObjectBigIntIdMock.from((String) null)
            );

            assertThat(exception).hasMessage("Cannot parse null string");
        }

        @Test
        public void shouldThrowOnTooLargeNumber()
        {
            String tooLarge = "9223372036854775808"; // Long.MAX_VALUE + 1

            NumberFormatException exception = Assertions.assertThrows(
                NumberFormatException.class,
                () -> ObjectBigIntIdMock.from(tooLarge)
            );

            assertThat(exception).hasMessageContaining("For input string: \"9223372036854775808\"");
        }

        @Test
        public void shouldThrowOnTooSmallNumber()
        {
            String tooSmall = "-9223372036854775809"; // Long.MIN_VALUE - 1

            NumberFormatException exception = Assertions.assertThrows(
                NumberFormatException.class,
                () -> ObjectBigIntIdMock.from(tooSmall)
            );

            assertThat(exception).hasMessageContaining("For input string: \"-9223372036854775809\"");
        }

    }

    @Nested
    final class LongConstruction
    {

        @Test
        public void shouldCreateFromLong()
        {
            long value = 123456789L;

            var result = ObjectBigIntIdMock.from(value);

            Assertions.assertNotNull(result);
            Assertions.assertEquals(value, result.toLong());
        }

        @Test
        public void shouldCreateFromZero()
        {
            var result = ObjectBigIntIdMock.from(0L);

            Assertions.assertNotNull(result);
            Assertions.assertEquals(0L, result.toLong());
        }

        @Test
        public void shouldCreateFromNegativeValue()
        {
            long value = -123456789L;

            var result = ObjectBigIntIdMock.from(value);

            Assertions.assertNotNull(result);
            Assertions.assertEquals(value, result.toLong());
        }

    }

    @Nested
    final class BigIntegerConversion
    {

        @Test
        public void shouldConvertToBigInteger()
        {
            long value = 693968158089838711L;
            ObjectBigIntIdMock id = ObjectBigIntIdMock.from(value);

            BigInteger result = id.toBigInteger();

            Assertions.assertEquals(BigInteger.valueOf(value), result);
        }

        @Test
        public void shouldConvertMaxLongToBigInteger()
        {
            ObjectBigIntIdMock id = ObjectBigIntIdMock.from(Long.MAX_VALUE);

            BigInteger result = id.toBigInteger();

            Assertions.assertEquals(BigInteger.valueOf(Long.MAX_VALUE), result);
        }

        @Test
        public void shouldConvertMinLongToBigInteger()
        {
            ObjectBigIntIdMock id = ObjectBigIntIdMock.from(Long.MIN_VALUE);

            BigInteger result = id.toBigInteger();

            Assertions.assertEquals(BigInteger.valueOf(Long.MIN_VALUE), result);
        }

    }

    @Nested
    final class CompareTo
    {

        @Test
        public void shouldCompareCorrectly()
        {
            ObjectBigIntIdMock id1 = ObjectBigIntIdMock.from(100L);
            ObjectBigIntIdMock id2 = ObjectBigIntIdMock.from(200L);

            int result = id1.compareTo(id2);

            assertThat(result).isNegative();
        }

        @Test
        public void shouldReturnZeroForEqualValues()
        {
            ObjectBigIntIdMock id1 = ObjectBigIntIdMock.from(100L);
            ObjectBigIntIdMock id2 = ObjectBigIntIdMock.from(100L);

            int result = id1.compareTo(id2);

            Assertions.assertEquals(0, result);
        }

        @Test
        public void shouldHandleNegativeComparison()
        {
            ObjectBigIntIdMock id1 = ObjectBigIntIdMock.from(-100L);
            ObjectBigIntIdMock id2 = ObjectBigIntIdMock.from(100L);

            int result = id1.compareTo(id2);

            assertThat(result).isNegative();
        }

    }

    @Nested
    final class HashCode
    {

        @Test
        public void shouldReturnSameHashCodeForEqualValues()
        {
            ObjectBigIntIdMock id1 = ObjectBigIntIdMock.from(123456789L);
            ObjectBigIntIdMock id2 = ObjectBigIntIdMock.from(123456789L);

            Assertions.assertEquals(id1.hashCode(), id2.hashCode());
        }

        @Test
        public void shouldReturnDifferentHashCodeForDifferentValues()
        {
            ObjectBigIntIdMock id1 = ObjectBigIntIdMock.from(123456789L);
            ObjectBigIntIdMock id2 = ObjectBigIntIdMock.from(987654321L);

            Assertions.assertNotEquals(id1.hashCode(), id2.hashCode());
        }

    }

    @Nested
    final class EqualsEdgeCases
    {

        @Test
        public void shouldNotEqualNull()
        {
            ObjectBigIntIdMock id = ObjectBigIntIdMock.from(123456789L);

            assertThat(id).isNotEqualTo(null);
        }

        @Test
        public void shouldNotEqualDifferentType()
        {
            ObjectBigIntIdMock id = ObjectBigIntIdMock.from(123456789L);

            assertThat(id).isNotEqualTo("string");
        }

        @Test
        public void shouldNotEqualDifferentSubtype()
        {
            ObjectBigIntIdMock id1 = ObjectBigIntIdMock.from(123456789L);
            ObjectBigIntIdMock2 id2 = ObjectBigIntIdMock2.from(123456789L);

            assertThat(id1).isNotEqualTo(id2);
            assertThat(id2).isNotEqualTo(id1);
        }

        @Test
        public void shouldNotEqualNonObjectBigIntId()
        {
            ObjectBigIntIdMock id = ObjectBigIntIdMock.from(123456789L);

            assertThat(id).isNotEqualTo(123456789L);
        }

    }

    @Nested
    final class Serialization
    {

        @Test
        public void shouldThrowOnJavaSerialization() throws Exception
        {
            ObjectBigIntIdMock id = ObjectBigIntIdMock.from(123456789L);
            try (var oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
                UnsupportedOperationException exception = Assertions.assertThrows(
                    UnsupportedOperationException.class,
                    () -> oos.writeObject(id)
                );

                assertThat(exception).hasMessage("Java Serialization is not supported. Do you have properly configured mapping for this type?");
            }
        }

    }

    public static class ObjectBigIntIdMock2 extends ObjectBigIntId<ObjectBigIntIdMock2>
    {

        private ObjectBigIntIdMock2(final long inner)
        {
            super(inner);
        }

        public static ObjectBigIntIdMock2 from(final long value)
        {
            return ObjectBigIntId.fromLong(ObjectBigIntIdMock2::new, value);
        }

    }

}
