package org.framefork.typedIds.spring.convert;

import org.framefork.typedIds.spring.convert.TestIds.BigIntId;
import org.framefork.typedIds.spring.convert.TestIds.InvalidBigIntId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class NumberToObjectBigIntIdConverterFactoryTest
{

    private final NumberToObjectBigIntIdConverterFactory factory = new NumberToObjectBigIntIdConverterFactory();

    @Test
    void convertsWholeNumbers()
    {
        var converter = factory.getConverter(BigIntId.class);

        assertThat(converter.convert(12345L)).isEqualTo(BigIntId.create(12345L));
        assertThat(converter.convert(42)).isEqualTo(BigIntId.create(42L));
        assertThat(converter.convert((short) 7)).isEqualTo(BigIntId.create(7L));
        assertThat(converter.convert(BigInteger.valueOf(Long.MAX_VALUE))).isEqualTo(BigIntId.create(Long.MAX_VALUE));
        assertThat(converter.convert(new BigDecimal("15.000"))).isEqualTo(BigIntId.create(15L));
        assertThat(converter.convert(3.0d)).isEqualTo(BigIntId.create(3L));
        assertThat(converter.convert(null)).isNull();
    }

    @Test
    void rejectsFractionsAndOverflowAsIllegalArgument()
    {
        var converter = factory.getConverter(BigIntId.class);

        assertThatThrownBy(() -> converter.convert(1.5d))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot convert \"1.5\" to " + BigIntId.class.getName());
        assertThatThrownBy(() -> converter.convert(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE)))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> converter.convert(Double.NaN))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsIdTypeWithoutConstructorWhenTheConverterIsRequested()
    {
        assertThatThrownBy(() -> factory.getConverter(InvalidBigIntId.class))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(InvalidBigIntId.class.getName());
    }

}
