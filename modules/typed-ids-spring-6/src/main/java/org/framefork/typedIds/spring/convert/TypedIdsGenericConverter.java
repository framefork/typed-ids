package org.framefork.typedIds.spring.convert;

import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.ObjectBigIntIdTypeUtils;
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

        Class<?> target = targetType.getObjectType();
        if (ObjectUuid.class.isAssignableFrom(target)) {
            return convertToUuidId(source, target);
        }
        if (ObjectBigIntId.class.isAssignableFrom(target)) {
            return convertToBigIntId(source, target);
        }

        throw new IllegalArgumentException("Cannot convert %s to %s".formatted(sourceType, targetType));
    }

    @Nullable
    private static Object convertToUuidId(final Object source, final Class<?> target)
    {
        var constructor = TypedIdConversions.uuidConstructor(target);
        if (source instanceof UUID uuid) {
            return ObjectUuidTypeUtils.wrapUuidToIdentifier(uuid, constructor);
        }
        if (source instanceof String string) {
            return string.isBlank() ? null : ObjectUuidTypeUtils.wrapUuidToIdentifier(TypedIdConversions.parseUuid(target, string), constructor);
        }

        throw new IllegalArgumentException("Cannot convert %s to %s".formatted(source.getClass().getName(), target.getName()));
    }

    @Nullable
    private static Object convertToBigIntId(final Object source, final Class<?> target)
    {
        var constructor = TypedIdConversions.bigIntConstructor(target);
        if (source instanceof Number number) {
            return ObjectBigIntIdTypeUtils.wrapBigIntToIdentifier(TypedIdConversions.toLong(target, number), constructor);
        }
        if (source instanceof String string) {
            return string.isBlank() ? null : ObjectBigIntIdTypeUtils.wrapBigIntToIdentifier(TypedIdConversions.parseLong(target, string), constructor);
        }

        throw new IllegalArgumentException("Cannot convert %s to %s".formatted(source.getClass().getName(), target.getName()));
    }

}
