package org.framefork.typedIds.spring.convert;

import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.ObjectBigIntIdTypeUtils;
import org.framefork.typedIds.common.TypedIdTypeUtils;
import org.framefork.typedIds.uuid.ObjectUuid;
import org.framefork.typedIds.uuid.ObjectUuidTypeUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.Set;
import java.util.UUID;

/**
 * All typed-id conversions in one {@link GenericConverter}: {@link String} and {@link UUID} to {@link ObjectUuid} subtypes,
 * {@link String} and {@link Number} to {@link ObjectBigIntId} subtypes, with the same rules as the four converter factories in this package.
 * <p>
 * It exists because Spring Boot registers {@link GenericConverter} beans with the MVC and WebFlux conversion services and, when qualified with
 * {@code @ConfigurationPropertiesBinding}, with configuration-properties binding on every Boot version, while {@code ConverterFactory} beans are picked up
 * only since Boot 3.5.
 */
public final class TypedIdsGenericConverter implements GenericConverter
{

    private static final Set<ConvertiblePair> CONVERTIBLE_TYPES = Set.of(
        new ConvertiblePair(String.class, ObjectUuid.class),
        new ConvertiblePair(UUID.class, ObjectUuid.class),
        new ConvertiblePair(String.class, ObjectBigIntId.class),
        new ConvertiblePair(Number.class, ObjectBigIntId.class)
    );

    @Override
    public Set<ConvertiblePair> getConvertibleTypes()
    {
        return CONVERTIBLE_TYPES;
    }

    @Nullable
    @Override
    public Object convert(@Nullable final Object source, final TypeDescriptor sourceType, final TypeDescriptor targetType)
    {
        if (source == null) {
            return null;
        }

        // the target is checked first, so an id class without the expected constructor fails even for blank input, like the factories do
        Class<?> target = targetType.getObjectType();
        if (ObjectUuid.class.isAssignableFrom(target)) {
            ObjectUuidTypeUtils.getMainConstructor(target);
        } else if (ObjectBigIntId.class.isAssignableFrom(target)) {
            ObjectBigIntIdTypeUtils.getMainConstructor(target);
        } else {
            throw new IllegalArgumentException("Cannot convert %s to %s".formatted(sourceType, targetType));
        }

        // a blank String converts to null, the Spring convention for an empty request parameter; core treats it as malformed
        if (source instanceof String string && string.isBlank()) {
            return null;
        }

        return TypedIdTypeUtils.convert(target, source);
    }

}
