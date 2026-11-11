package com.example;

import java.util.UUID;

public final class TenantUserId extends AbstractTenantUuidId<TenantUserId>
{

    private TenantUserId(final UUID inner)
    {
        super(inner);
    }

}
