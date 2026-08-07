package org.framefork.typedIds.spring.convert;

import org.framefork.typedIds.common.ReflectionHacks;
import org.framefork.typedIds.uuid.ObjectUuid;
import org.framefork.typedIds.uuid.ObjectUuidTypeUtils;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.lang.invoke.MethodHandle;
import java.util.UUID;

/**
 * Generic converter factory that converts {@link String} to any {@link ObjectUuid} subtype.
 *
 * <p>This factory eliminates the need for individual converter classes for each Id type.
 * It works by using {@link MethodHandle} to invoke the private constructor that all
 * {@link ObjectUuid} subtypes have.
 *
 * <p>To use this converter, register it with Spring's conversion service:
 * <pre>{@code
 * @Configuration
 * public class ConversionServiceConfiguration {
 *     @Bean
 *     public ConversionService conversionService() {
 *         DefaultConversionService service = new DefaultConversionService();
 *         service.addConverterFactory(new StringToObjectUuidConverterFactory());
 *         return service;
 *     }
 * }
 * }</pre>
 */
public class StringToObjectUuidConverterFactory implements ConverterFactory<String, ObjectUuid<?>> {

    @Override
    public <T extends ObjectUuid<?>> @NotNull Converter<String, T> getConverter(final @NotNull Class<T> targetType) {
        return new StringToObjectUuidConverter<>(targetType);
    }

    private static final class StringToObjectUuidConverter<T extends ObjectUuid<?>>
            implements Converter<String, T> {

        private final Class<T> targetType;

        private StringToObjectUuidConverter(final Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public @Nullable T convert(final @Nullable String source) {
            if (source == null || source.isEmpty()) {
                return null;
            }

            try {
                final UUID uuid = UUID.fromString(source);
                final var constructor = ReflectionHacks.getConstructor(targetType, UUID.class);
                @SuppressWarnings("unchecked")
                var result = (T) ObjectUuidTypeUtils.wrapUuidToIdentifier(uuid, constructor);
                return result;
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Cannot convert String to " + targetType.getName() +
                        ". Ensure it extends ObjectUuid and has a private constructor taking a UUID parameter.",
                        e
                );
            }
        }
    }

}