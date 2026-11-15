package org.framefork.typedIds.spring.convert;

import org.framefork.typedIds.spring.convert.TestIds.InvalidUuidId;
import org.framefork.typedIds.spring.convert.TestIds.UuidId;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class StringToObjectUuidConverterFactoryTest
{

    private final StringToObjectUuidConverterFactory factory = new StringToObjectUuidConverterFactory();

    @Test
    void convertsString()
    {
        var converter = factory.getConverter(UuidId.class);

        assertThat(converter.convert(TestIds.UUID_V7)).isEqualTo(UuidId.parse(TestIds.UUID_V7));
    }

    @Test
    void returnsNullForNullOrBlankInput()
    {
        var converter = factory.getConverter(UuidId.class);

        assertThat(converter.convert(null)).isNull();
        assertThat(converter.convert("")).isNull();
        assertThat(converter.convert("  ")).isNull();
    }

    @Test
    void rejectsMalformedInputAsIllegalArgument()
    {
        var converter = factory.getConverter(UuidId.class);

        assertThatThrownBy(() -> converter.convert("not-a-uuid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot convert \"not-a-uuid\" to " + UuidId.class.getName());
    }

    @Test
    void rejectsIdTypeWithoutConstructorWhenTheConverterIsRequested()
    {
        assertThatThrownBy(() -> factory.getConverter(InvalidUuidId.class))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(InvalidUuidId.class.getName());
    }

}
