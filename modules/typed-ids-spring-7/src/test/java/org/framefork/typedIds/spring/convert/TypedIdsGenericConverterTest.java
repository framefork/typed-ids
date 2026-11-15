package org.framefork.typedIds.spring.convert;

import org.framefork.typedIds.spring.convert.TestIds.BigIntId;
import org.framefork.typedIds.spring.convert.TestIds.InvalidBigIntId;
import org.framefork.typedIds.spring.convert.TestIds.UuidId;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.support.DefaultConversionService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class TypedIdsGenericConverterTest
{

    private final DefaultConversionService conversionService = new DefaultConversionService();

    TypedIdsGenericConverterTest()
    {
        conversionService.addConverter(new TypedIdsGenericConverter());
    }

    @Test
    void convertsEverySupportedSource()
    {
        assertThat(conversionService.convert(TestIds.UUID_V7, UuidId.class)).isEqualTo(UuidId.parse(TestIds.UUID_V7));
        assertThat(conversionService.convert(UUID.fromString(TestIds.UUID_V7), UuidId.class)).isEqualTo(UuidId.parse(TestIds.UUID_V7));
        assertThat(conversionService.convert("42", BigIntId.class)).isEqualTo(BigIntId.create(42L));
        assertThat(conversionService.convert(42, BigIntId.class)).isEqualTo(BigIntId.create(42L));
        assertThat(conversionService.convert(42L, BigIntId.class)).isEqualTo(BigIntId.create(42L));
        assertThat(conversionService.convert("", UuidId.class)).isNull();
    }

    @Test
    void nothingElseInSpringConvertsTheTestIds()
    {
        var withoutTypedIds = new DefaultConversionService();

        assertThat(withoutTypedIds.canConvert(String.class, UuidId.class)).isFalse();
        assertThat(withoutTypedIds.canConvert(UUID.class, UuidId.class)).isFalse();
        assertThat(withoutTypedIds.canConvert(String.class, BigIntId.class)).isFalse();
        assertThat(withoutTypedIds.canConvert(Long.class, BigIntId.class)).isFalse();
    }

    @Test
    void reportsMalformedInputAsIllegalArgument()
    {
        assertThatThrownBy(() -> conversionService.convert("not-a-uuid", UuidId.class))
            .isInstanceOf(ConversionFailedException.class)
            .hasCauseInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> conversionService.convert("abc", BigIntId.class))
            .isInstanceOf(ConversionFailedException.class)
            .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reportsIdTypeWithoutConstructorAsIllegalStateEvenForBlankInput()
    {
        assertThatThrownBy(() -> conversionService.convert(" ", InvalidBigIntId.class))
            .isInstanceOf(ConversionFailedException.class)
            .hasCauseInstanceOf(IllegalStateException.class);
    }

    @Test
    void reportsIdTypeWithoutConstructorAsIllegalState()
    {
        assertThatThrownBy(() -> conversionService.convert("42", InvalidBigIntId.class))
            .isInstanceOf(ConversionFailedException.class)
            .hasCauseInstanceOf(IllegalStateException.class);
    }

}
