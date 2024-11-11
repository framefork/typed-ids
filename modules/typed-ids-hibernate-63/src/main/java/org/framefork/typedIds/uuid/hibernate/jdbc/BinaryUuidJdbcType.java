package org.framefork.typedIds.uuid.hibernate.jdbc;

import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.hibernate.type.descriptor.jdbc.VarbinaryJdbcType;

public class BinaryUuidJdbcType extends VarbinaryJdbcType
{

    public static final BinaryUuidJdbcType INSTANCE = new BinaryUuidJdbcType();

    @Override
    public String getFriendlyName()
    {
        return "uuid-bin(16)";
    }

    @Override
    public int getJdbcTypeCode()
    {
        return SqlTypes.UUID;
    }

    @Override
    protected boolean shouldUseMaterializedLob(final JdbcTypeIndicators indicators)
    {
        return false;
    }

    @Override
    public boolean isLob()
    {
        return false;
    }

    @Override
    public boolean isLobOrLong()
    {
        return false;
    }

    @Override
    public String toString()
    {
        return getFriendlyName();
    }

}
