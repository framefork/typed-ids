package org.framefork.typedIds.spring.convert;

import org.framefork.typedIds.spring.convert.TestIds.BigIntId;
import org.framefork.typedIds.spring.convert.TestIds.InvalidBigIntId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class StringToObjectBigIntIdConverterFactoryTest
{

    private final StringToObjectBigIntIdConverterFactory factory = new StringToObjectBigIntIdConverterFactory();

    @Test
    void convertsString()
    {
        var converter = factory.getConverter(BigIntId.class);

        assertThat(converter.convert("42")).isEqualTo(BigIntId.create(42L));
        assertThat(converter.convert(" 9223372036854775807 ")).isEqualTo(BigIntId.create(Long.MAX_VALUE));
        assertThat(converter.convert(null)).isNull();
        assertThat(converter.convert(" ")).isNull();
    }

    @Test
    void rejectsMalformedInputAsIllegalArgument()
    {
        var converter = factory.getConverter(BigIntId.class);

        assertThatThrownBy(() -> converter.convert("abc"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot convert \"abc\" to " + BigIntId.class.getName());
        assertThatThrownBy(() -> converter.convert("9223372036854775808"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsIdTypeWithoutConstructorWhenTheConverterIsRequested()
    {
        assertThatThrownBy(() -> factory.getConverter(InvalidBigIntId.class))
            .isInstanceOf(IllegalStateException.class);
    }

}
