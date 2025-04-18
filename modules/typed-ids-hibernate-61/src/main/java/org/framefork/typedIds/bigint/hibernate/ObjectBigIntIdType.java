package org.framefork.typedIds.bigint.hibernate;

import io.hypersistence.utils.hibernate.type.ImmutableType;
import io.hypersistence.utils.hibernate.type.util.ParameterizedParameterType;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.ObjectBigIntIdTypeUtils;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metamodel.model.domain.DomainType;
import org.hibernate.query.BindableType;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.java.MutabilityPlan;
import org.hibernate.type.descriptor.java.MutabilityPlanExposer;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.spi.TypeConfiguration;
import org.hibernate.type.spi.TypeConfigurationAware;
import org.hibernate.usertype.DynamicParameterizedType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

public class ObjectBigIntIdType extends ImmutableType<ObjectBigIntId<?>> implements
    BindableType<ObjectBigIntId<?>>,
    DomainType<ObjectBigIntId<?>>,
    MutabilityPlanExposer<ObjectBigIntId<?>>,
    DynamicParameterizedType,
    TypeConfigurationAware
{

    public static final String NAME = "object-bigint-id";

    private final ObjectBigIntIdJavaType javaTypeDescriptor = new ObjectBigIntIdJavaType();

    @Nullable
    private JdbcType jdbcTypeDescriptor;

    @Nullable
    private TypeConfiguration typeConfiguration;

    public ObjectBigIntIdType()
    {
        this((JdbcType) null);
    }

    public ObjectBigIntIdType(@Nullable final JdbcType longJdbcType)
    {
        super(ObjectBigIntIdTypeUtils.getObjectBigIntIdRawClass());
        this.jdbcTypeDescriptor = longJdbcType;
    }

    public ObjectBigIntIdType(final Class<?> implClass)
    {
        this(implClass, null);
    }

    @SuppressWarnings("this-escape")
    public ObjectBigIntIdType(final Class<?> implClass, @Nullable final JdbcType longJdbcType)
    {
        this(longJdbcType);

        var parameters = new Properties();
        parameters.put(DynamicParameterizedType.PARAMETER_TYPE, new ParameterizedParameterType(implClass));
        this.setParameterValues(parameters);
    }

    @Override
    public void setTypeConfiguration(final TypeConfiguration typeConfiguration)
    {
        this.typeConfiguration = typeConfiguration;
    }

    @Nullable
    @Override
    public TypeConfiguration getTypeConfiguration()
    {
        return typeConfiguration;
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

    @Override
    public long getDefaultSqlLength(final Dialect dialect, final JdbcType jdbcType)
    {
        return javaTypeDescriptor.getDefaultSqlLength(dialect, jdbcType);
    }

    @NonNull
    public JdbcType getJdbcType()
    {
        return Objects.requireNonNull(jdbcTypeDescriptor, "jdbcTypeDescriptor is not yet initialized");
    }

    @Override
    public ObjectBigIntIdJavaType getExpressibleJavaType()
    {
        return javaTypeDescriptor;
    }

    @Override
    public void setParameterValues(final Properties parameters)
    {
        javaTypeDescriptor.setParameterValues(parameters);
        if (jdbcTypeDescriptor == null && typeConfiguration != null) {
            jdbcTypeDescriptor = typeConfiguration.getJdbcTypeRegistry().getDescriptor(getSqlType());
        }
    }

    @Override
    public Class<ObjectBigIntId<?>> returnedClass()
    {
        return javaTypeDescriptor.getJavaTypeClass();
    }

    @Override
    public Class<ObjectBigIntId<?>> getBindableJavaType()
    {
        return returnedClass();
    }

    @Nullable
    @Override
    public ObjectBigIntId<?> fromStringValue(@Nullable final CharSequence sequence)
    {
        return javaTypeDescriptor.fromString(sequence);
    }

    @Override
    public MutabilityPlan<ObjectBigIntId<?>> getExposedMutabilityPlan()
    {
        return javaTypeDescriptor.getMutabilityPlan();
    }

    public ObjectBigIntId<?> wrapJdbcValue(final Object value)
    {
        if (value instanceof Long longValue) {
            return javaTypeDescriptor.wrap(longValue, null);
        }
        if (value instanceof Number numberValue) {
            return wrapJdbcValue(numberValue.longValue());
        }
        if (getReturnedClass().isInstance(value)) {
            return getReturnedClass().cast(value);
        }

        throw new HibernateException("Could not convert '%s' to '%s'".formatted(value.getClass().getName(), getReturnedClass()));
    }

    /**
     * the nullSafeGet() is delegated here in the {@link ImmutableType}
     */
    @Nullable
    @Override
    protected ObjectBigIntId<?> get(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException
    {
        return getJdbcType().getExtractor(javaTypeDescriptor).extract(rs, position, session);
    }

    /**
     * the nullSafeSet() is delegated here in the {@link ImmutableType}
     */
    @Override
    protected void set(PreparedStatement st, @Nullable ObjectBigIntId<?> value, int index, SharedSessionContractImplementor session) throws SQLException
    {
        getJdbcType().getBinder(javaTypeDescriptor).bind(st, value, index, session);
    }

    @Override
    public String toString()
    {
        return "%s backed by %s".formatted(javaTypeDescriptor, jdbcTypeDescriptor != null ? jdbcTypeDescriptor : "???");
    }

}
