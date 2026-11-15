package org.framefork.typedIds.bigint;

import org.framefork.typedIds.common.ReflectionHacks;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.math.BigDecimal;
import java.math.BigInteger;

@ApiStatus.Internal
public final class ObjectBigIntIdTypeUtils
{

    private static final ClassValue<MethodHandle> MAIN_CONSTRUCTORS = new ClassValue<>()
    {
        @Override
        protected MethodHandle computeValue(final Class<?> idType)
        {
            if (!ObjectBigIntId.class.isAssignableFrom(idType)) {
                throw new IllegalArgumentException("Type %s is not a subtype of %s".formatted(idType.getName(), ObjectBigIntId.class.getName()));
            }

            try {
                return ReflectionHacks.getConstructor(idType, long.class);

            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(
                    "Cannot convert to %s: it must declare a constructor taking a single long, like every typed id does".formatted(idType.getName()),
                    e
                );
            }
        }
    };

    private ObjectBigIntIdTypeUtils()
    {
    }

    public static ObjectBigIntId<?> wrapBigIntToIdentifier(final long id, MethodHandle mainConstructor)
    {
        try {
            return getObjectBigIntIdRawClass().cast(mainConstructor.invoke(id));

        } catch (RuntimeException e) {
            throw e;

        } catch (Throwable e) {
            String typeName = mainConstructor.type().returnType().getName();
            throw new IllegalArgumentException(String.format("Failed construction of %s using (\"%s\")", typeName, id), e);
        }
    }

    /**
     * Returns the constructor taking a single {@code long} that every {@link ObjectBigIntId} subtype has, looked up once per type.
     *
     * @throws IllegalArgumentException when the type is not an {@link ObjectBigIntId} subtype
     * @throws IllegalStateException when the type lacks the constructor, which is a programming error in the id class
     */
    public static MethodHandle getMainConstructor(final Class<?> idType)
    {
        return MAIN_CONSTRUCTORS.get(idType);
    }

    /**
     * Converts a {@link Number} or a {@link String} holding a whole number within the {@code long} range to the given {@link ObjectBigIntId} subtype.
     * <p>
     * A number with a fraction, or outside the {@code long} range, is malformed input, never truncated. Surrounding whitespace in the string is ignored;
     * a blank string is malformed input.
     *
     * @throws IllegalArgumentException when the source cannot become the id: a malformed string, a fraction, an overflow, or another source type
     * @throws IllegalStateException when the id type lacks the constructor taking a single {@code long}
     */
    public static <T extends ObjectBigIntId<?>> T convert(final Class<T> idType, final Object source)
    {
        var constructor = getMainConstructor(idType);
        if (source instanceof Number number) {
            return idType.cast(wrapBigIntToIdentifier(toLong(idType, number), constructor));
        }
        if (source instanceof String string) {
            return idType.cast(wrapBigIntToIdentifier(parseLong(idType, string), constructor));
        }

        throw new IllegalArgumentException("Cannot convert %s to %s".formatted(source.getClass().getName(), idType.getName()));
    }

    private static long parseLong(final Class<?> idType, final String value)
    {
        try {
            return Long.parseLong(value.trim());

        } catch (NumberFormatException e) {
            throw malformed(idType, value, e);
        }
    }

    private static long toLong(final Class<?> idType, final Number value)
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
            throw malformed(idType, value, e);
        }
    }

    private static IllegalArgumentException malformed(final Class<?> idType, final Object value, final Exception cause)
    {
        return new IllegalArgumentException("Cannot convert \"%s\" to %s".formatted(value, idType.getName()), cause);
    }

    @SuppressWarnings("unchecked")
    public static Class<ObjectBigIntId<?>> getObjectBigIntIdRawClass()
    {
        return (Class<ObjectBigIntId<?>>) (Class<?>) ObjectBigIntId.class;
    }

}
