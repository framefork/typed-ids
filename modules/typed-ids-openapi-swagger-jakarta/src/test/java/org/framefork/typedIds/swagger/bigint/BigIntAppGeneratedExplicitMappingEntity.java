package org.framefork.typedIds.swagger.bigint;

import org.framefork.typedIds.bigint.ObjectBigIntId;

public class BigIntAppGeneratedExplicitMappingEntity
{

    private final Id id;
    private final String title;

    public BigIntAppGeneratedExplicitMappingEntity(final String title)
    {
        this.id = Id.random();
        this.title = title;
    }

    public Id getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public static final class Id extends ObjectBigIntId<Id>
    {

        private Id(final long inner)
        {
            super(inner);
        }

        public static Id random()
        {
            return ObjectBigIntId.randomBigInt(Id::new);
        }

        public static Id from(final String value)
        {
            return ObjectBigIntId.fromString(Id::new, value);
        }

        public static Id from(final long value)
        {
            return ObjectBigIntId.fromLong(Id::new, value);
        }

    }

}
