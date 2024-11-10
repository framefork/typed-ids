package org.framefork.typedIds.uuid;

import java.util.UUID;

public final class UserId extends ObjectUuid<UserId>
{

    private UserId(final UUID inner)
    {
        super(inner);
    }

    public static UserId random()
    {
        return ObjectUuid.randomUUID(UserId::new);
    }

    public static UserId fromString(final String value)
    {
        return ObjectUuid.fromString(UserId::new, value);
    }

    public static UserId fromUuid(final UUID value)
    {
        return ObjectUuid.fromUuid(UserId::new, value);
    }

}
