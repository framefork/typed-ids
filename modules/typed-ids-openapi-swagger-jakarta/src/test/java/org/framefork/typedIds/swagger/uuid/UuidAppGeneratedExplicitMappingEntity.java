package org.framefork.typedIds.swagger.uuid;

import org.framefork.typedIds.uuid.ObjectUuid;

import java.util.UUID;

public class UuidAppGeneratedExplicitMappingEntity
{

    private final Id id;
    private final String title;

    public UuidAppGeneratedExplicitMappingEntity(final String title)
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

    public static final class Id extends ObjectUuid<Id>
    {

        private Id(final UUID inner)
        {
            super(inner);
        }

        public static Id random()
        {
            return ObjectUuid.randomUUID(Id::new);
        }

        public static Id from(final String value)
        {
            return ObjectUuid.fromString(Id::new, value);
        }

        public static Id from(final UUID value)
        {
            return ObjectUuid.fromUuid(Id::new, value);
        }

    }

}
