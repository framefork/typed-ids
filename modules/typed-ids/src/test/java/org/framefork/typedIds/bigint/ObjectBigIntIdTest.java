package org.framefork.typedIds.bigint;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

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

            Assertions.assertSame(expected, leftId.equals(rightId));
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

}
