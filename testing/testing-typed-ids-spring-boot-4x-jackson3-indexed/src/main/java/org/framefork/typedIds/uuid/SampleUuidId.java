package org.framefork.typedIds.uuid;

import java.util.UUID;

public final class SampleUuidId extends ObjectUuid<SampleUuidId>
{

    private SampleUuidId(final UUID inner)
    {
        super(inner);
    }

    public static SampleUuidId random()
    {
        return ObjectUuid.randomUUID(SampleUuidId::new);
    }

    public static SampleUuidId from(final String value)
    {
        return ObjectUuid.fromString(SampleUuidId::new, value);
    }

    public static SampleUuidId from(final UUID value)
    {
        return ObjectUuid.fromUuid(SampleUuidId::new, value);
    }

}
