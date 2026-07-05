package org.framefork.typedIds.bigint;

public final class BigIntAppGeneratedExplicitMappingEntityId extends ObjectBigIntId<BigIntAppGeneratedExplicitMappingEntityId>
{

    private BigIntAppGeneratedExplicitMappingEntityId(final long inner)
    {
        super(inner);
    }

    public static BigIntAppGeneratedExplicitMappingEntityId random()
    {
        return ObjectBigIntId.randomBigInt(BigIntAppGeneratedExplicitMappingEntityId::new);
    }

    public static BigIntAppGeneratedExplicitMappingEntityId from(final String value)
    {
        return ObjectBigIntId.fromString(BigIntAppGeneratedExplicitMappingEntityId::new, value);
    }

    public static BigIntAppGeneratedExplicitMappingEntityId from(final long value)
    {
        return ObjectBigIntId.fromLong(BigIntAppGeneratedExplicitMappingEntityId::new, value);
    }

}
