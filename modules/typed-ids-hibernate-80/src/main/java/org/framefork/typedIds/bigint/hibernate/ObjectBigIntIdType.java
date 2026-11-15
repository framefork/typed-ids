package org.framefork.typedIds.bigint.hibernate;

import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.common.hibernate.ImmutableType;
import org.framefork.typedIds.common.hibernate.ParameterizedTypeUtils;
import org.hibernate.HibernateException;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.jspecify.annotations.Nullable;

public class ObjectBigIntIdType extends ImmutableType<ObjectBigIntId<?>, ObjectBigIntIdJavaType>
{

    public static final String NAME = "object-bigint-id";

    public ObjectBigIntIdType()
    {
        this((JdbcType) null);
    }

    public ObjectBigIntIdType(@Nullable final JdbcType longJdbcType)
    {
        super(new ObjectBigIntIdJavaType(), longJdbcType);
    }

    public ObjectBigIntIdType(final Class<?> implClass)
    {
        this(implClass, null);
    }

    @SuppressWarnings("this-escape")
    public ObjectBigIntIdType(final Class<?> implClass, @Nullable final JdbcType longJdbcType)
    {
        this(longJdbcType);
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
        return SqlTypes.BIGINT;
    }

    public ObjectBigIntId<?> wrapJdbcValue(final Object value)
    {
        if (value instanceof Long longValue) {
            return getExpressibleJavaType().wrap(longValue, null);
        }
        if (value instanceof Number numberValue) {
            return wrapJdbcValue(numberValue.longValue());
        }
        if (getReturnedClass().isInstance(value)) {
            return getReturnedClass().cast(value);
        }

        throw new HibernateException("Could not convert '%s' to '%s'".formatted(value.getClass().getName(), getReturnedClass()));
    }

}
