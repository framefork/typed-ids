package org.framefork.typedIds.uuid;

import org.framefork.typedIds.common.ReflectionHacks;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.UUID;

@ApiStatus.Internal
public final class ObjectUuidTypeUtils
{

    private static final int UUID_STRING_LENGTH = 36;

    private static final ClassValue<MethodHandle> MAIN_CONSTRUCTORS = new ClassValue<>()
    {
        @Override
        protected MethodHandle computeValue(final Class<?> idType)
        {
            if (!ObjectUuid.class.isAssignableFrom(idType)) {
                throw new IllegalArgumentException("Type %s is not a subtype of %s".formatted(idType.getName(), ObjectUuid.class.getName()));
            }

            try {
                return ReflectionHacks.getConstructor(idType, UUID.class);

            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(
                    "Cannot convert to %s: it must declare a constructor taking a single %s, like every typed id does".formatted(idType.getName(), UUID.class.getName()),
                    e
                );
            }
        }
    };

    private ObjectUuidTypeUtils()
    {
    }

    public static ObjectUuid<?> wrapUuidToIdentifier(final UUID uuid, MethodHandle mainConstructor)
    {
        try {
            return getObjectUuidRawClass().cast(mainConstructor.invoke(uuid));

        } catch (RuntimeException e) {
            throw e;

        } catch (Throwable e) {
            String typeName = mainConstructor.type().returnType().getName();
            throw new IllegalArgumentException(String.format("Failed construction of %s using (\"%s\")", typeName, uuid), e);
        }
    }

    /**
     * Returns the constructor taking a single {@link UUID} that every {@link ObjectUuid} subtype has, looked up once per type.
     *
     * @throws IllegalArgumentException when the type is not an {@link ObjectUuid} subtype
     * @throws IllegalStateException when the type lacks the constructor, which is a programming error in the id class
     */
    public static MethodHandle getMainConstructor(final Class<?> idType)
    {
        return MAIN_CONSTRUCTORS.get(idType);
    }

    /**
     * Converts a {@link UUID}, or a {@link String} in the canonical 36-character UUID form, to the given {@link ObjectUuid} subtype.
     * <p>
     * Surrounding whitespace in the string is ignored. A blank string is malformed input.
     *
     * @throws IllegalArgumentException when the source cannot become the id: a malformed string, a UUID version the id rejects, or another source type
     * @throws IllegalStateException when the id type lacks the constructor taking a single {@link UUID}
     */
    public static <T extends ObjectUuid<?>> T convert(final Class<T> idType, final Object source)
    {
        var constructor = getMainConstructor(idType);
        if (source instanceof UUID uuid) {
            return idType.cast(wrapUuidToIdentifier(uuid, constructor));
        }
        if (source instanceof String string) {
            return idType.cast(wrapUuidToIdentifier(parseUuid(idType, string), constructor));
        }

        throw new IllegalArgumentException("Cannot convert %s to %s".formatted(source.getClass().getName(), idType.getName()));
    }

    /**
     * {@link UUID#fromString(String)} alone also accepts shortened groups like {@code 1-1-1-1-1}, so the length is checked first.
     */
    private static UUID parseUuid(final Class<?> idType, final String value)
    {
        var trimmed = value.trim();
        if (trimmed.length() != UUID_STRING_LENGTH) {
            throw malformed(idType, value, null);
        }

        try {
            return UUID.fromString(trimmed);

        } catch (IllegalArgumentException e) {
            throw malformed(idType, value, e);
        }
    }

    private static IllegalArgumentException malformed(final Class<?> idType, final String value, @Nullable final Exception cause)
    {
        return new IllegalArgumentException("Cannot convert \"%s\" to %s".formatted(value, idType.getName()), cause);
    }

    @SuppressWarnings("unchecked")
    public static Class<ObjectUuid<?>> getObjectUuidRawClass()
    {
        return (Class<ObjectUuid<?>>) (Class<?>) ObjectUuid.class;
    }

}
