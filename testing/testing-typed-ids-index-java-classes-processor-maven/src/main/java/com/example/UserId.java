package com.example;

import org.framefork.typedIds.uuid.ObjectUuid;

import java.util.UUID;

public final class UserId extends ObjectUuid<UserId>
{

    private UserId(final UUID inner)
    {
        super(inner);
    }

}
