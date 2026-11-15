package org.framefork.typedIds.spring.convert;

import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.lang.invoke.MethodHandle;

/**
 * Converts a whole {@link Number} within the long range to any {@link ObjectBigIntId} subtype, through the private constructor every typed id has.
 * <p>
 * The constructor is looked up once per target type, when Spring asks for the converter, so an id type without it fails right there with an
 * {@link IllegalStateException}. Input that cannot become the id fails the conversion with an {@link IllegalArgumentException}.
 * Under Spring Boot, the auto-configuration in this module registers all typed-id conversions; register this factory yourself only without Boot:
 * <pre>{@code
 * conversionService.addConverterFactory(new NumberToObjectBigIntIdConverterFactory());
 * }</pre>
 */
public final class NumberToObjectBigIntIdConverterFactory implements ConverterFactory<Number, ObjectBigIntId<?>>
{

    @Override
    public <T extends ObjectBigIntId<?>> Converter<Number, T> getConverter(final Class<T> targetType)
    {
        return new TypedIdConverter<>(targetType, TypedIdConversions.bigIntConstructor(targetType));
    }

    private static final class TypedIdConverter<T extends ObjectBigIntId<?>> implements Converter<Number, T>
    {

        private final Class<T> targetType;
        private final MethodHandle constructor;

        private TypedIdConverter(final Class<T> targetType, final MethodHandle constructor)
        {
            this.targetType = targetType;
            this.constructor = constructor;
        }

        @Nullable
        @Override
        public T convert(@Nullable final Number source)
        {
            if (source == null) {
                return null;
            }

            return TypedIdConversions.bigIntId(targetType, constructor, TypedIdConversions.toLong(targetType, source));
        }

    }

}
