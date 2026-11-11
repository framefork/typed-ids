package com.example;

import org.framefork.typedIds.bigint.ObjectBigIntId;

public abstract class AbstractTenantBigIntId<SelfType extends AbstractTenantBigIntId<SelfType>> extends ObjectBigIntId<SelfType>
{

    protected AbstractTenantBigIntId(final long inner)
    {
        super(inner);
    }

}
