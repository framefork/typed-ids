package org.framefork.typedIds.spring.convert;

import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.ObjectBigIntIdTypeUtils;
import org.framefork.typedIds.common.ReflectionHacks;
import org.framefork.typedIds.uuid.ObjectUuid;
import org.framefork.typedIds.uuid.ObjectUuidTypeUtils;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.UUID;

/**
 * The conversions shared by the converter factories and {@link TypedIdsGenericConverter}.
 * <p>
 * Two kinds of failure are kept apart. A value that cannot become the id (a malformed UUID, a number out of the long range, a UUID version the id rejects)
 * is an {@link IllegalArgumentException}, which Spring reports as a conversion failure of the input. An id type without the expected private constructor
 * is a programming error and an {@link IllegalStateException}, raised as soon as a converter for that type is requested.
 */
final class TypedIdConversions
{

    private static final int UUID_STRING_LENGTH = 36;

    private static final ClassValue<MethodHandle> UUID_CONSTRUCTORS = new ClassValue<>()
    {
        @Override
        protected MethodHandle computeValue(final Class<?> type)
        {
            return resolveConstructor(type, UUID.class);
        }
    };

    private static final ClassValue<MethodHandle> BIGINT_CONSTRUCTORS = new ClassValue<>()
    {
        @Override
        protected MethodHandle computeValue(final Class<?> type)
        {
            return resolveConstructor(type, long.class);
        }
    };

    private TypedIdConversions()
    {
    }

    static MethodHandle uuidConstructor(final Class<?> type)
    {
        return UUID_CONSTRUCTORS.get(type);
    }

    static MethodHandle bigIntConstructor(final Class<?> type)
    {
        return BIGINT_CONSTRUCTORS.get(type);
    }

    static <T extends ObjectUuid<?>> T uuidId(final Class<T> type, final MethodHandle constructor, final UUID value)
    {
        return type.cast(ObjectUuidTypeUtils.wrapUuidToIdentifier(value, constructor));
    }

    static <T extends ObjectBigIntId<?>> T bigIntId(final Class<T> type, final MethodHandle constructor, final long value)
    {
        return type.cast(ObjectBigIntIdTypeUtils.wrapBigIntToIdentifier(value, constructor));
    }

    /**
     * Parses the canonical 36-character UUID form only; {@link UUID#fromString(String)} alone also accepts shortened groups like {@code 1-1-1-1-1}.
     */
    static UUID parseUuid(final Class<?> type, final String value)
    {
        var trimmed = value.trim();
        if (trimmed.length() != UUID_STRING_LENGTH) {
            throw malformed(type, value, null);
        }

        try {
            return UUID.fromString(trimmed);

        } catch (IllegalArgumentException e) {
            throw malformed(type, value, e);
        }
    }

    static long parseLong(final Class<?> type, final String value)
    {
        try {
            return Long.parseLong(value.trim());

        } catch (NumberFormatException e) {
            throw malformed(type, value, e);
        }
    }

    /**
     * Accepts any {@link Number} whose value is a whole number within the long range; fractions and overflow are malformed input, not silently truncated.
     */
    static long toLong(final Class<?> type, final Number value)
    {
        if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
            return value.longValue();
        }

        try {
            if (value instanceof BigInteger bigInteger) {
                return bigInteger.longValueExact();
            }
            if (value instanceof BigDecimal bigDecimal) {
                return bigDecimal.longValueExact();
            }
            return new BigDecimal(value.toString()).longValueExact();

        } catch (ArithmeticException | NumberFormatException e) {
            throw malformed(type, value, e);
        }
    }

    private static MethodHandle resolveConstructor(final Class<?> type, final Class<?> parameterType)
    {
        try {
            return ReflectionHacks.getConstructor(type, parameterType);

        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                "Cannot convert to %s: it must declare a constructor taking a single %s, like every typed id does".formatted(type.getName(), parameterType.getName()),
                e
            );
        }
    }

    private static IllegalArgumentException malformed(final Class<?> type, final Object value, @Nullable final Exception cause)
    {
        return new IllegalArgumentException("Cannot convert \"%s\" to %s".formatted(value, type.getName()), cause);
    }

}
