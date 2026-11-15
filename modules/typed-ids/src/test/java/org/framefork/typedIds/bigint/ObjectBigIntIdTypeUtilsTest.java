package org.framefork.typedIds.bigint;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class ObjectBigIntIdTypeUtilsTest
{

    @Test
    void convertsWholeNumbers()
    {
        assertThat(ObjectBigIntIdTypeUtils.convert(ObjectBigIntIdMock.class, 12345L)).isEqualTo(ObjectBigIntIdMock.from(12345L));
        assertThat(ObjectBigIntIdTypeUtils.convert(ObjectBigIntIdMock.class, 42)).isEqualTo(ObjectBigIntIdMock.from(42L));
        assertThat(ObjectBigIntIdTypeUtils.convert(ObjectBigIntIdMock.class, (short) 7)).isEqualTo(ObjectBigIntIdMock.from(7L));
        assertThat(ObjectBigIntIdTypeUtils.convert(ObjectBigIntIdMock.class, BigInteger.valueOf(Long.MAX_VALUE))).isEqualTo(ObjectBigIntIdMock.from(Long.MAX_VALUE));
        assertThat(ObjectBigIntIdTypeUtils.convert(ObjectBigIntIdMock.class, new BigDecimal("15.000"))).isEqualTo(ObjectBigIntIdMock.from(15L));
        assertThat(ObjectBigIntIdTypeUtils.convert(ObjectBigIntIdMock.class, 3.0d)).isEqualTo(ObjectBigIntIdMock.from(3L));
    }

    @Test
    void convertsStrings()
    {
        assertThat(ObjectBigIntIdTypeUtils.convert(ObjectBigIntIdMock.class, "42")).isEqualTo(ObjectBigIntIdMock.from(42L));
        assertThat(ObjectBigIntIdTypeUtils.convert(ObjectBigIntIdMock.class, " 9223372036854775807 ")).isEqualTo(ObjectBigIntIdMock.from(Long.MAX_VALUE));
    }

    @Test
    void rejectsFractionsAndOverflowAsIllegalArgument()
    {
        assertThatThrownBy(() -> ObjectBigIntIdTypeUtils.convert(ObjectBigIntIdMock.class, 1.5d))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot convert \"1.5\" to " + ObjectBigIntIdMock.class.getName());
        assertThatThrownBy(() -> ObjectBigIntIdTypeUtils.convert(ObjectBigIntIdMock.class, BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE)))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ObjectBigIntIdTypeUtils.convert(ObjectBigIntIdMock.class, Double.NaN))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsMalformedStringsAsIllegalArgument()
    {
        assertThatThrownBy(() -> ObjectBigIntIdTypeUtils.convert(ObjectBigIntIdMock.class, "abc"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot convert \"abc\" to " + ObjectBigIntIdMock.class.getName());
        assertThatThrownBy(() -> ObjectBigIntIdTypeUtils.convert(ObjectBigIntIdMock.class, "9223372036854775808"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ObjectBigIntIdTypeUtils.convert(ObjectBigIntIdMock.class, " "))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ObjectBigIntIdTypeUtils.convert(ObjectBigIntIdMock.class, new Object()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsIdTypeWithoutConstructorAsIllegalState()
    {
        assertThatThrownBy(() -> ObjectBigIntIdTypeUtils.getMainConstructor(InvalidBigIntId.class))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(InvalidBigIntId.class.getName());
        assertThatThrownBy(() -> ObjectBigIntIdTypeUtils.convert(InvalidBigIntId.class, 42L))
            .isInstanceOf(IllegalStateException.class);
    }

    /**
     * Lacks the private long constructor every typed id has.
     */
    static final class InvalidBigIntId extends ObjectBigIntId<InvalidBigIntId>
    {

        @SuppressWarnings({"UnusedMethod", "UnusedVariable"})
        private InvalidBigIntId(final String invalid)
        {
            super(0);
        }

    }

}
