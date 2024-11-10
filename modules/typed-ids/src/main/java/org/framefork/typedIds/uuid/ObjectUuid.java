package org.framefork.typedIds.uuid;

import com.google.errorprone.annotations.Immutable;
import org.atteo.classindex.IndexSubclasses;
import org.framefork.typedIds.TypedId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * Wraps {@link UUID}
 */
@IndexSubclasses
@Immutable
public abstract class ObjectUuid<SelfType extends ObjectUuid<SelfType>> implements TypedId<SelfType>
{

    private static final int ALLOWED_LEGACY_VERSION = 4;
    private static final int ALLOWED_CURRENT_VERSION = 7;

    @Serial
    private static final long serialVersionUID = 2024_11_01_08_00_00L;

    private final UUID inner;

    protected ObjectUuid(final UUID inner)
    {
        Objects.requireNonNull(inner, "inner must not be null");

        int version = inner.version();
        if (ALLOWED_LEGACY_VERSION != version && ALLOWED_CURRENT_VERSION != version) {
            throw new IllegalArgumentException(String.format("Only versions [%d, %d] are supported, bud version %d was given", ALLOWED_LEGACY_VERSION, ALLOWED_CURRENT_VERSION, version));
        }

        this.inner = inner;
    }

    @NotNull
    protected static <SelfType extends ObjectUuid<SelfType>> SelfType randomUUID(
        final Function<UUID, SelfType> constructor
    )
    {
        return constructor.apply(ObjectUuidGenerator.randomUUid(constructor));
    }

    @NotNull
    protected static <SelfType extends ObjectUuid<SelfType>> SelfType fromString(
        final Function<UUID, SelfType> constructor,
        final String name
    )
    {
        return fromUuid(constructor, UUID.fromString(name));
    }

    @NotNull
    protected static <SelfType extends ObjectUuid<SelfType>> SelfType fromUuid(
        final Function<UUID, SelfType> constructor,
        final UUID uuid
    )
    {
        return constructor.apply(uuid);
    }

    @NotNull
    public UUID toNativeUuid()
    {
        return inner;
    }

    /**
     * Returns the least significant 64 bits of this UUID's 128 bit value.
     *
     * @return The least significant 64 bits of this UUID's 128 bit value
     */
    public long getLeastSignificantBits()
    {
        return inner.getLeastSignificantBits();
    }

    /**
     * Returns the most significant 64 bits of this UUID's 128 bit value.
     *
     * @return The most significant 64 bits of this UUID's 128 bit value
     */
    public long getMostSignificantBits()
    {
        return inner.getMostSignificantBits();
    }

    /**
     * The version number associated with this {@code ObjectUuid}.  The version
     * number describes how this {@code ObjectUuid} was generated.
     * <p>
     * The version number has the following meaning:
     * <ul>
     * <li>1    Time-based UUID
     * <li>2    DCE security UUID
     * <li>3    Name-based UUID
     * <li>4    Randomly generated UUID
     * </ul>
     *
     * @return The version number of this {@code ObjectUuid}
     */
    public int version()
    {
        return inner.version();
    }

    /**
     * The variant number associated with this {@code ObjectUuid}.  The variant
     * number describes the layout of the {@code ObjectUuid}.
     * <p>
     * The variant number has the following meaning:
     * <ul>
     * <li>0    Reserved for NCS backward compatibility
     * <li>2    <a href="http://www.ietf.org/rfc/rfc4122.txt">IETF&nbsp;RFC&nbsp;4122</a>
     * (Leach-Salz), used by this class
     * <li>6    Reserved, Microsoft Corporation backward compatibility
     * <li>7    Reserved for future definition
     * </ul>
     *
     * @return The variant number of this {@code ObjectUuid}
     */
    public int variant()
    {
        return inner.variant();
    }

    /**
     * Compares this UUID with the specified UUID.
     *
     * <p> The first of two UUIDs is greater than the second if the most
     * significant field in which the UUIDs differ is greater for the first
     * UUID.
     *
     * @param other {@code ObjectUuid} to which this {@code ObjectUuid} is to be compared
     * @return -1, 0 or 1 as this {@code ObjectUuid} is less than, equal to, or greater than {@code other}
     */
    @Override
    public int compareTo(@NotNull final SelfType other)
    {
        return this.toNativeUuid().compareTo(other.toNativeUuid());
    }

    /**
     * Returns a {@code String} object representing this {@code ObjectUuid}.
     *
     * <p> The UUID string representation is as described by this BNF:
     * <blockquote><pre>
     * {@code
     * UUID                   = <time_low> "-" <time_mid> "-"
     *                          <time_high_and_version> "-"
     *                          <variant_and_sequence> "-"
     *                          <node>
     * time_low               = 4*<hexOctet>
     * time_mid               = 2*<hexOctet>
     * time_high_and_version  = 2*<hexOctet>
     * variant_and_sequence   = 2*<hexOctet>
     * node                   = 6*<hexOctet>
     * hexOctet               = <hexDigit><hexDigit>
     * hexDigit               =
     *       "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9"
     *       | "a" | "b" | "c" | "d" | "e" | "f"
     *       | "A" | "B" | "C" | "D" | "E" | "F"
     * }</pre></blockquote>
     *
     * @return A string representation of this {@code ObjectUuid}
     */
    @Override
    public String toString()
    {
        return inner.toString();
    }

    @Override
    public boolean equals(@Nullable final Object o)
    {
        if (!(o instanceof ObjectUuid)) {
            return false;
        }
        if (!this.getClass().isInstance(o)) {
            return false;
        }
        @SuppressWarnings("unchecked")
        var that = (ObjectUuid<SelfType>) o;

        return inner.equals(that.inner);
    }

    @Override
    public int hashCode()
    {
        return inner.hashCode();
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

}
