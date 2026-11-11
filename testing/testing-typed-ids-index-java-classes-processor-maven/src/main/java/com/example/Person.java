package com.example;

import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.uuid.ObjectUuid;

import java.util.UUID;

public record Person(Id id, LegacyId legacyId, String name)
{

    public static final class Id extends ObjectUuid<Id>
    {

        private Id(final UUID inner)
        {
            super(inner);
        }

    }

    public static final class LegacyId extends ObjectBigIntId<LegacyId>
    {

        private LegacyId(final long inner)
        {
            super(inner);
        }

    }

}
