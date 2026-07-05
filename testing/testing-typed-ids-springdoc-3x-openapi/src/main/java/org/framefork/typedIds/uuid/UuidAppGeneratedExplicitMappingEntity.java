package org.framefork.typedIds.uuid;

public class UuidAppGeneratedExplicitMappingEntity
{

    private final UuidAppGeneratedExplicitMappingEntityId id;
    private final String title;

    public UuidAppGeneratedExplicitMappingEntity(final String title)
    {
        this.id = UuidAppGeneratedExplicitMappingEntityId.random();
        this.title = title;
    }

    public UuidAppGeneratedExplicitMappingEntityId getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

}
