package org.framefork.typedIds.spring.convert;

import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.uuid.ObjectUuid;

import java.util.UUID;

public final class TestIds
{

    public static final String UUID_V7 = "01936d8a-7b5e-7c3f-9a1b-2c3d4e5f6a7b";
    public static final String UUID_V1 = "550e8400-e29b-11d4-a716-446655440000";

    private TestIds()
    {
    }

    public static final class UuidId extends ObjectUuid<UuidId>
    {

        private UuidId(final UUID inner)
        {
            super(inner);
        }

        // not named valueOf/of/from: Spring's ObjectToObjectConverter would use such a method to convert a String without the typed-ids converter
        public static UuidId parse(final String value)
        {
            return ObjectUuid.fromString(UuidId::new, value);
        }

    }

    public static final class BigIntId extends ObjectBigIntId<BigIntId>
    {

        private BigIntId(final long inner)
        {
            super(inner);
        }

        // not named valueOf/of/from, for the same reason as UuidId.parse
        public static BigIntId create(final long value)
        {
            return ObjectBigIntId.fromLong(BigIntId::new, value);
        }

    }

    /**
     * Lacks the private UUID constructor every typed id has.
     */
    public static final class InvalidUuidId extends ObjectUuid<InvalidUuidId>
    {

        @SuppressWarnings({"UnusedMethod", "UnusedVariable"})
        private InvalidUuidId(final String invalid)
        {
            super(UUID.fromString(UUID_V7));
        }

    }

    /**
     * Lacks the private long constructor every typed id has.
     */
    public static final class InvalidBigIntId extends ObjectBigIntId<InvalidBigIntId>
    {

        @SuppressWarnings({"UnusedMethod", "UnusedVariable"})
        private InvalidBigIntId(final String invalid)
        {
            super(0);
        }

    }

}
