package org.framefork.typedIds.uuid.hibernate;

import io.hypersistence.utils.hibernate.type.ImmutableType;
import io.hypersistence.utils.hibernate.type.util.ParameterizedParameterType;
import org.framefork.typedIds.uuid.ObjectUuid;
import org.framefork.typedIds.uuid.ObjectUuidTypeUtils;
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

public class ObjectUuidType extends ImmutableType<ObjectUuid<?>> implements
    BindableType<ObjectUuid<?>>,
    DomainType<ObjectUuid<?>>,
    MutabilityPlanExposer<ObjectUuid<?>>,
    DynamicParameterizedType,
    TypeConfigurationAware
{

    public static final String NAME = "object-uuid";

    private final ObjectUuidJavaType javaTypeDescriptor = new ObjectUuidJavaType();

    @Nullable
    private JdbcType jdbcTypeDescriptor;

    @Nullable
    private TypeConfiguration typeConfiguration;

    public ObjectUuidType()
    {
        this((JdbcType) null);
    }

    public ObjectUuidType(@Nullable final JdbcType uuidJdbcType)
    {
        super(ObjectUuidTypeUtils.getObjectUuidRawClass());
        this.jdbcTypeDescriptor = uuidJdbcType;
    }

    public ObjectUuidType(final Class<?> implClass)
    {
        this(implClass, null);
    }

    public ObjectUuidType(final Class<?> implClass, @Nullable final JdbcType uuidJdbcType)
    {
        this(uuidJdbcType);

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
        return SqlTypes.UUID;
    }

    @Override
    public long getDefaultSqlLength(final Dialect dialect, final JdbcType jdbcType)
    {
        return javaTypeDescriptor.getDefaultSqlLength(dialect, jdbcType);
    }

    @NonNull
    @Override
    public JdbcType getJdbcType(final TypeConfiguration typeConfiguration)
    {
        return Objects.requireNonNull(jdbcTypeDescriptor, "jdbcTypeDescriptor is not yet initialized");
    }

    @Override
    public ObjectUuidJavaType getExpressibleJavaType()
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
    public Class<ObjectUuid<?>> returnedClass()
    {
        return javaTypeDescriptor.getJavaTypeClass();
    }

    @Override
    public Class<ObjectUuid<?>> getBindableJavaType()
    {
        return returnedClass();
    }

    @Nullable
    @Override
    public ObjectUuid<?> fromStringValue(@Nullable final CharSequence sequence)
    {
        return javaTypeDescriptor.fromString(sequence);
    }

    @Override
    public MutabilityPlan<ObjectUuid<?>> getExposedMutabilityPlan()
    {
        return javaTypeDescriptor.getMutabilityPlan();
    }

    /**
     * the nullSafeGet() is delegated here in the {@link ImmutableType}
     */
    @Nullable
    @Override
    protected ObjectUuid<?> get(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException
    {
        return getJdbcType(session.getTypeConfiguration())
            .getExtractor(javaTypeDescriptor).extract(rs, position, session);
    }

    /**
     * the nullSafeSet() is delegated here in the {@link ImmutableType}
     */
    @Override
    protected void set(PreparedStatement st, @Nullable ObjectUuid<?> value, int index, SharedSessionContractImplementor session) throws SQLException
    {
        getJdbcType(session.getTypeConfiguration())
            .getBinder(javaTypeDescriptor).bind(st, value, index, session);
    }

    @Override
    public String toString()
    {
        return "%s backed by %s".formatted(javaTypeDescriptor, jdbcTypeDescriptor != null ? jdbcTypeDescriptor : "???");
    }

}
