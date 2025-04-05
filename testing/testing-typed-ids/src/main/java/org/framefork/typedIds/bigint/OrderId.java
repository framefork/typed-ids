package org.framefork.typedIds.bigint;

public final class OrderId extends ObjectBigIntId<OrderId>
{

    private OrderId(final long inner)
    {
        super(inner);
    }

    public static OrderId random()
    {
        return ObjectBigIntId.randomBigInt(OrderId::new);
    }

    public static OrderId from(final String value)
    {
        return ObjectBigIntId.fromString(OrderId::new, value);
    }

    public static OrderId from(final long value)
    {
        return ObjectBigIntId.fromLong(OrderId::new, value);
    }

}
