package com.example;

import org.framefork.typedIds.uuid.ObjectUuid;

import java.util.UUID;

public abstract class AbstractTenantUuidId<SelfType extends AbstractTenantUuidId<SelfType>> extends ObjectUuid<SelfType>
{

    protected AbstractTenantUuidId(final UUID inner)
    {
        super(inner);
    }

}
