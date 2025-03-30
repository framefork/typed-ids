package org.framefork.typedIds.uuid;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
            ObjectUuidMock id = ObjectUuidMock.random();
            Assertions.assertNotNull(id);
            assertThat(id.version()).isEqualTo(7);
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
            ObjectUuidMock leftId = ObjectUuidMock.from(left);
            ObjectUuidMock rightId = ObjectUuidMock.from(right);

            Assertions.assertSame(expected, leftId.equals(rightId));
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

}
