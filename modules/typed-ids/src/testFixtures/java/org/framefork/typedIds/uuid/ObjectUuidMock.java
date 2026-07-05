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

    public static ObjectUuidMock from(final String value)
    {
        return ObjectUuid.fromString(ObjectUuidMock::new, value);
    }

    public static ObjectUuidMock from(final UUID value)
    {
        return ObjectUuid.fromUuid(ObjectUuidMock::new, value);
    }

}
