package org.framefork.typedIds.uuid.hibernate.jdbc;

import org.hibernate.type.SqlTypes;
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
    public String toString()
    {
        return getFriendlyName();
    }

}
