package org.framefork.typedIds.uuid.hibernate.jdbc;

import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.UUIDJdbcType;

public class NativeUuidJdbcType extends UUIDJdbcType
{

    public static final NativeUuidJdbcType INSTANCE = new NativeUuidJdbcType();

    @Override
    public String getFriendlyName()
    {
        return "uuid";
    }

    @Override
    public int getJdbcTypeCode()
    {
        return SqlTypes.UUID;
    }

    @Override
    public int getDefaultSqlTypeCode()
    {
        return SqlTypes.UUID;
    }

    @Override
    public String toString()
    {
        return "uuid-native";
    }

}
