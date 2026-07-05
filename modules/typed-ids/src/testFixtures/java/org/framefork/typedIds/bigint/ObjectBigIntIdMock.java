package org.framefork.typedIds.bigint;

public final class ObjectBigIntIdMock extends ObjectBigIntId<ObjectBigIntIdMock>
{

    private ObjectBigIntIdMock(final long inner)
    {
        super(inner);
    }

    public static ObjectBigIntIdMock random()
    {
        return ObjectBigIntId.randomBigInt(ObjectBigIntIdMock::new);
    }

    public static ObjectBigIntIdMock from(final String value)
    {
        return ObjectBigIntId.fromString(ObjectBigIntIdMock::new, value);
    }

    public static ObjectBigIntIdMock from(final long value)
    {
        return ObjectBigIntId.fromLong(ObjectBigIntIdMock::new, value);
    }

}
