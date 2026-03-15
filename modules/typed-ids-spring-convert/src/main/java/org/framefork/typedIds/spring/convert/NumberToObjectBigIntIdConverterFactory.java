package org.framefork.typedIds.spring.convert;

import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.ObjectBigIntIdTypeUtils;
import org.framefork.typedIds.common.ReflectionHacks;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

import java.lang.invoke.MethodHandle;

/**
 * Generic converter factory that converts {@link Number} to any {@link ObjectBigIntId} subtype.
 *
 * <p>This factory eliminates the need for individual converter classes for each Id type.
 * It works by using {@link MethodHandle} to invoke the private constructor that all
 * {@link ObjectBigIntId} subtypes have.
 *
 * <p>To use this converter, register it with Spring's conversion service:
 * <pre>{@code
 * @Configuration
 * public class ConversionServiceConfiguration {
 *     @Bean
 *     public ConversionService conversionService() {
 *         DefaultConversionService service = new DefaultConversionService();
 *         service.addConverterFactory(new NumberToObjectBigIntIdConverterFactory());
 *         return service;
 *     }
 * }
 * }</pre>
 */
public class NumberToObjectBigIntIdConverterFactory implements ConverterFactory<Number, ObjectBigIntId<?>> {

    @Override
    public <T extends ObjectBigIntId<?>> @NotNull Converter<Number, T> getConverter(final @NotNull Class<T> targetType) {
        return new NumberToObjectBigIntIdConverter<>(targetType);
    }

    private static final class NumberToObjectBigIntIdConverter<T extends ObjectBigIntId<?>>
            implements Converter<Number, T> {

        private final Class<T> targetType;

        private NumberToObjectBigIntIdConverter(final Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public @Nullable T convert(final @Nullable Number source) {
            if (source == null) {
                return null;
            }

            try {
                final var constructor = ReflectionHacks.getConstructor(targetType, long.class);
                @SuppressWarnings("unchecked")
                var result = (T) ObjectBigIntIdTypeUtils.wrapBigIntToIdentifier(source.longValue(), constructor);
                return result;
            } catch (final IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Cannot convert Number to " + targetType.getName() +
                        ". Ensure it extends ObjectBigIntId and has a private constructor taking a long parameter.",
                        e
                );
            }
        }
    }

}