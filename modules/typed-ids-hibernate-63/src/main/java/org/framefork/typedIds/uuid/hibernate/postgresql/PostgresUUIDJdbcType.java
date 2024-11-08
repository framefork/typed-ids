package org.framefork.typedIds.uuid.hibernate.postgresql;

import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.UUIDJdbcType;

public class PostgresUUIDJdbcType extends UUIDJdbcType
{

    public static final PostgresUUIDJdbcType INSTANCE = new PostgresUUIDJdbcType();

    @Override
    public String getFriendlyName()
    {
        return "pg-uuid";
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

}
