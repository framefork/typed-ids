package org.framefork.typedIds.bigint;

public final class SampleBigIntId extends ObjectBigIntId<SampleBigIntId>
{

    private SampleBigIntId(final long inner)
    {
        super(inner);
    }

    public static SampleBigIntId random()
    {
        return ObjectBigIntId.randomBigInt(SampleBigIntId::new);
    }

    public static SampleBigIntId from(final String value)
    {
        return ObjectBigIntId.fromString(SampleBigIntId::new, value);
    }

    public static SampleBigIntId from(final long value)
    {
        return ObjectBigIntId.fromLong(SampleBigIntId::new, value);
    }

}
