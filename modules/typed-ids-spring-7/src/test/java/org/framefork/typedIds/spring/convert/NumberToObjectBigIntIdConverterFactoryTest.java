package org.framefork.typedIds.spring.convert;

import org.framefork.typedIds.spring.convert.TestIds.BigIntId;
import org.framefork.typedIds.spring.convert.TestIds.InvalidBigIntId;
import org.junit.jupiter.api.Test;

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
        assertThat(converter.convert(null)).isNull();
    }

    @Test
    void rejectsMalformedInputAsIllegalArgument()
    {
        var converter = factory.getConverter(BigIntId.class);

        assertThatThrownBy(() -> converter.convert(1.5d))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot convert \"1.5\" to " + BigIntId.class.getName());
    }

    @Test
    void rejectsIdTypeWithoutConstructorWhenTheConverterIsRequested()
    {
        assertThatThrownBy(() -> factory.getConverter(InvalidBigIntId.class))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(InvalidBigIntId.class.getName());
    }

}
