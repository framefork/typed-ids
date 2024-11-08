package org.framefork.typedIds.uuid;

import java.util.UUID;

public final class ObjectUuidMock extends ObjectUuid<ObjectUuidMock>
{

    private ObjectUuidMock(final UUID inner)
    {
        super(inner);
    }

    public static ObjectUuidMock random()
    {
        return ObjectUuid.randomUUID(ObjectUuidMock::new);
    }

    public static ObjectUuidMock fromString(final String name)
    {
        return ObjectUuid.fromString(ObjectUuidMock::new, name);
    }

    public static ObjectUuidMock fromUuid(final UUID uuid)
    {
        return ObjectUuid.fromUuid(ObjectUuidMock::new, uuid);
    }

}
