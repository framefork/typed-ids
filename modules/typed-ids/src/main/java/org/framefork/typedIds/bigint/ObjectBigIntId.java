package org.framefork.typedIds.bigint;

import com.google.errorprone.annotations.Immutable;
import org.framefork.typedIds.TypedId;
import org.framefork.typedIds.common.LazyValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Wraps long
 */
@Immutable
public abstract class ObjectBigIntId<SelfType extends ObjectBigIntId<SelfType>> implements TypedId<SelfType>
{

    private final long inner;

    protected ObjectBigIntId(final long inner)
    {
        this.inner = inner;
    }

    protected static <SelfType extends ObjectBigIntId<SelfType>> SelfType randomBigInt(
        final Constructor<SelfType> constructor
    )
    {
        return constructor.apply(Generators.randomBigInt(constructor));
    }

    protected static <SelfType extends ObjectBigIntId<SelfType>> SelfType fromString(
        final Constructor<SelfType> constructor,
        final String name
    )
    {
        return fromLong(constructor, Long.parseLong(name));
    }

    protected static <SelfType extends ObjectBigIntId<SelfType>> SelfType fromLong(
        final Constructor<SelfType> constructor,
        final long id
    )
    {
        return constructor.apply(id);
    }

    public long toLong()
    {
        return inner;
    }

    public BigInteger toBigInteger()
    {
        return BigInteger.valueOf(inner);
    }

    @Override
    public int compareTo(@NonNull final SelfType other)
    {
        return Long.compare(this.inner, other.toLong());
    }

    @Override
    public String toString()
    {
        return Long.toString(inner);
    }

    @Override
    public boolean equals(@Nullable final Object o)
    {
        if (!(o instanceof ObjectBigIntId<?> that)) {
            return false;
        }
        if (!this.getClass().isInstance(o)) { // compare subtypes
            return false;
        }
        return this.toLong() == that.toLong();
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(inner);
    }

    @SuppressWarnings("unused")
    private void writeObject(final ObjectOutputStream stream)
    {
        throw new UnsupportedOperationException("Java Serialization is not supported. Do you have properly configured mapping for this type?");
    }

    @SuppressWarnings("unused")
    private void readObject(final ObjectInputStream stream)
    {
        throw new UnsupportedOperationException("Java Serialization is not supported. Do you have properly configured mapping for this type?");
    }

    public interface Constructor<SelfType extends ObjectBigIntId<SelfType>>
    {

        SelfType apply(long id);

    }

    /**
     * This class exists for two reasons: 1) to allow reusing generator instances per-type 2) to allow configuring a different generators factory.
     * This could be useful, if you want e.g. deterministic IDs in your tests.
     */
    public static final class Generators
    {

        private static final LazyValue<BigIntGenerator.Factory> FACTORY = new LazyValue<>(BigIntGenerator.Factory::getDefault);
        private static final ConcurrentHashMap<Constructor<?>, BigIntGenerator> GENERATORS = new ConcurrentHashMap<>();

        private Generators()
        {
        }

        static long randomBigInt(final Constructor<?> constructor)
        {
            return getGenerator(constructor).generateRandom();
        }

        static BigIntGenerator getGenerator(final Constructor<?> constructor)
        {
            return GENERATORS.computeIfAbsent(
                constructor,
                c -> FACTORY.get().getGenerator(c)
            );
        }

        /**
         * Replaces the factory with provided implementation and throws away any pre-existing generators.
         */
        public static void setFactory(final BigIntGenerator.Factory factory)
        {
            FACTORY.set(factory);
            GENERATORS.clear();
        }

    }

}
