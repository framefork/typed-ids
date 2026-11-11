package com.example;

import org.framefork.typedIds.bigint.ObjectBigIntId;

public final class OrderId extends ObjectBigIntId<OrderId>
{

    private OrderId(final long inner)
    {
        super(inner);
    }

}
