package com.example;

import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.uuid.ObjectUuid;

import java.util.UUID;

public final class Warehouse
{

    private Warehouse()
    {
    }

    public static final class Id extends ObjectUuid<Id>
    {

        private Id(final UUID inner)
        {
            super(inner);
        }

    }

    public static final class ShelfId extends ObjectBigIntId<ShelfId>
    {

        private ShelfId(final long inner)
        {
            super(inner);
        }

    }

}
