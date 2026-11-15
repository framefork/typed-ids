package org.framefork.typedIds.spring.convert;

import org.framefork.typedIds.spring.convert.TestIds.InvalidUuidId;
import org.framefork.typedIds.spring.convert.TestIds.UuidId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class UuidToObjectUuidConverterFactoryTest
{

    private final UuidToObjectUuidConverterFactory factory = new UuidToObjectUuidConverterFactory();

    @Test
    void convertsUuid()
    {
        var converter = factory.getConverter(UuidId.class);

        assertThat(converter.convert(UUID.fromString(TestIds.UUID_V7))).isEqualTo(UuidId.parse(TestIds.UUID_V7));
        assertThat(converter.convert(null)).isNull();
    }

    @Test
    void rejectsUnsupportedUuidVersionAsIllegalArgument()
    {
        var converter = factory.getConverter(UuidId.class);

        assertThatThrownBy(() -> converter.convert(UUID.fromString(TestIds.UUID_V1)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsIdTypeWithoutConstructorWhenTheConverterIsRequested()
    {
        assertThatThrownBy(() -> factory.getConverter(InvalidUuidId.class))
            .isInstanceOf(IllegalStateException.class);
    }

}
