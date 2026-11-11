package com.example;

import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.uuid.ObjectUuid;

import java.util.UUID;

public final class Catalog
{

    private Catalog()
    {
    }

    public static final class Section
    {

        private Section()
        {
        }

        public record Item(Id id, Sku sku)
        {

            public static final class Id extends ObjectUuid<Id>
            {

                private Id(final UUID inner)
                {
                    super(inner);
                }

            }

            public static final class Sku extends ObjectBigIntId<Sku>
            {

                private Sku(final long inner)
                {
                    super(inner);
                }

            }

        }

    }

}
