package org.framefork.typedIds.uuid;

import java.util.UUID;

public final class UuidAppGeneratedExplicitMappingEntityId extends ObjectUuid<UuidAppGeneratedExplicitMappingEntityId>
{

    private UuidAppGeneratedExplicitMappingEntityId(final UUID inner)
    {
        super(inner);
    }

    public static UuidAppGeneratedExplicitMappingEntityId random()
    {
        return ObjectUuid.randomUUID(UuidAppGeneratedExplicitMappingEntityId::new);
    }

    public static UuidAppGeneratedExplicitMappingEntityId from(final String value)
    {
        return ObjectUuid.fromString(UuidAppGeneratedExplicitMappingEntityId::new, value);
    }

    public static UuidAppGeneratedExplicitMappingEntityId from(final UUID value)
    {
        return ObjectUuid.fromUuid(UuidAppGeneratedExplicitMappingEntityId::new, value);
    }

}
