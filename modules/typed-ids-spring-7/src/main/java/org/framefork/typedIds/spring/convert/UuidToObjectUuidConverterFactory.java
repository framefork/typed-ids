package org.framefork.typedIds.spring.convert;

import org.framefork.typedIds.uuid.ObjectUuid;
import org.framefork.typedIds.uuid.ObjectUuidTypeUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.util.UUID;

/**
 * Converts a {@link UUID} to any {@link ObjectUuid} subtype, through the private constructor every typed id has.
 * <p>
 * The constructor is looked up once per target type, when Spring asks for the converter, so an id type without it fails right there with an
 * {@link IllegalStateException}. Input that cannot become the id fails the conversion with an {@link IllegalArgumentException}.
 * Under Spring Boot, the auto-configuration in this module registers all typed-id conversions; register this factory yourself only without Boot:
 * <pre>{@code
 * conversionService.addConverterFactory(new UuidToObjectUuidConverterFactory());
 * }</pre>
 */
public final class UuidToObjectUuidConverterFactory implements ConverterFactory<UUID, ObjectUuid<?>>
{

    @Override
    public <T extends ObjectUuid<?>> Converter<UUID, T> getConverter(final Class<T> targetType)
    {
        ObjectUuidTypeUtils.getMainConstructor(targetType); // an id class without the constructor fails here, when Spring asks for the converter
        return new TypedIdConverter<>(targetType);
    }

    private static final class TypedIdConverter<T extends ObjectUuid<?>> implements Converter<UUID, @Nullable T>
    {

        private final Class<T> targetType;

        private TypedIdConverter(final Class<T> targetType)
        {
            this.targetType = targetType;
        }

        @Nullable
        @Override
        public T convert(@Nullable final UUID source)
        {
            if (source == null) {
                return null;
            }

            return ObjectUuidTypeUtils.convert(targetType, source);
        }

    }

}
