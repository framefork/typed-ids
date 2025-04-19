package org.framefork.typedIds.uuid.hibernate;

import org.framefork.typedIds.common.hibernate.ImmutableType;
import org.framefork.typedIds.common.hibernate.ParameterizedTypeUtils;
import org.framefork.typedIds.uuid.ObjectUuid;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.jspecify.annotations.Nullable;

public class ObjectUuidType extends ImmutableType<ObjectUuid<?>, ObjectUuidJavaType>
{

    public static final String NAME = "object-uuid";

    public ObjectUuidType()
    {
        this((JdbcType) null);
    }

    public ObjectUuidType(@Nullable final JdbcType uuidJdbcType)
    {
        super(new ObjectUuidJavaType(), uuidJdbcType);
    }

    public ObjectUuidType(final Class<?> implClass)
    {
        this(implClass, null);
    }

    @SuppressWarnings("this-escape")
    public ObjectUuidType(final Class<?> implClass, @Nullable final JdbcType uuidJdbcType)
    {
        this(uuidJdbcType);
        this.setParameterValues(ParameterizedTypeUtils.forClass(implClass));
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public int getSqlType()
    {
        return SqlTypes.UUID;
    }

}
