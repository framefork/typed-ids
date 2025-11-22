package org.framefork.typedIds.bigint;

public class BigIntAppGeneratedExplicitMappingEntity
{

    private final BigIntAppGeneratedExplicitMappingEntityId id;
    private final String title;

    public BigIntAppGeneratedExplicitMappingEntity(final String title)
    {
        this.id = BigIntAppGeneratedExplicitMappingEntityId.random();
        this.title = title;
    }

    public BigIntAppGeneratedExplicitMappingEntityId getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

}
