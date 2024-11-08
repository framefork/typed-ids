package org.framefork.typedIds.uuid.hibernate;

import io.hypersistence.utils.hibernate.type.DescriptorImmutableType;
import io.hypersistence.utils.hibernate.type.util.ParameterizedParameterType;
import org.framefork.typedIds.uuid.ObjectUuid;
import org.framefork.typedIds.uuid.ObjectUuidTypeUtils;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.java.MutabilityPlan;
import org.hibernate.type.descriptor.java.MutabilityPlanExposer;
import org.hibernate.type.descriptor.jdbc.UUIDJdbcType;
import org.hibernate.usertype.DynamicParameterizedType;
import org.jetbrains.annotations.Nullable;

import java.util.Properties;

public class ObjectUuidType extends DescriptorImmutableType<ObjectUuid<?>, UUIDJdbcType, ObjectUuidJavaType> implements DynamicParameterizedType, MutabilityPlanExposer<ObjectUuid<?>>
{

    public static final String NAME = "objectUuid";

    public ObjectUuidType()
    {
        this(UUIDJdbcType.INSTANCE);
    }

    public ObjectUuidType(final UUIDJdbcType uuidJdbcType)
    {
        super(
            ObjectUuidTypeUtils.getObjectUuidRawClass(),
            uuidJdbcType,
            new ObjectUuidJavaType()
        );
    }

    public ObjectUuidType(final Class<?> implClass)
    {
        this(implClass, UUIDJdbcType.INSTANCE);
    }

    public ObjectUuidType(final Class<?> implClass, final UUIDJdbcType uuidJdbcType)
    {
        this(uuidJdbcType);

        var parameters = new Properties();
        parameters.put(DynamicParameterizedType.PARAMETER_TYPE, new ParameterizedParameterType(implClass));
        this.setParameterValues(parameters);
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
    public void setParameterValues(final Properties parameters)
    {
        getExpressibleJavaType().setParameterValues(parameters);
    }

    @Override
    public Class<ObjectUuid<?>> returnedClass()
    {
        return getExpressibleJavaType().getJavaTypeClass();
    }

    @Nullable
    @Override
    public ObjectUuid<?> fromStringValue(@Nullable final CharSequence sequence)
    {
        return getExpressibleJavaType().fromString(sequence);
    }

    @Override
    public ObjectUuidJavaType getExpressibleJavaType()
    {
        return (ObjectUuidJavaType) super.getExpressibleJavaType();
    }

    @Override
    public MutabilityPlan<ObjectUuid<?>> getExposedMutabilityPlan()
    {
        return ImmutableMutabilityPlan.instance();
    }

}
