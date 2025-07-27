package org.framefork.typedIds.bigint.hibernate;

import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.ObjectBigIntIdTypeUtils;
import org.framefork.typedIds.common.ReflectionHacks;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.BasicJavaType;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.java.LongJavaType;
import org.hibernate.type.descriptor.java.MutabilityPlan;
import org.hibernate.type.descriptor.jdbc.AdjustableJdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.hibernate.usertype.DynamicParameterizedType;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Properties;

public class ObjectBigIntIdJavaType implements BasicJavaType<ObjectBigIntId<?>>, DynamicParameterizedType, Serializable
{

    private final LongJavaType inner;

    @Nullable
    private Class<ObjectBigIntId<?>> identifierClass;
    @Nullable
    private MethodHandle constructor;

    public ObjectBigIntIdJavaType()
    {
        this.inner = LongJavaType.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setParameterValues(final Properties parameters)
    {
        var parameterType = (ParameterType) parameters.get(PARAMETER_TYPE);
        if (parameterType != null) {
            this.identifierClass = (Class<ObjectBigIntId<?>>) parameterType.getReturnedClass();

        } else {
            String entityClass = Objects.requireNonNull(parameters.get(ENTITY), "parameters.get(ENTITY) must not be null").toString();
            String propertyName = Objects.requireNonNull(parameters.get(PROPERTY), "parameters.get(PROPERTY) must not be null").toString();

            this.identifierClass = ReflectionHacks.getFieldTypeChecked(entityClass, propertyName, ObjectBigIntId.class);
        }

        if (!ObjectBigIntId.class.isAssignableFrom(identifierClass)) {
            throw new IllegalArgumentException("Type %s is not a subtype of %s".formatted(identifierClass, ObjectBigIntId.class));
        }

        this.constructor = ReflectionHacks.getConstructor(identifierClass, long.class);
    }

    @Override
    public Type getJavaType()
    {
        return getJavaTypeClass();
    }

    @Override
    public Class<ObjectBigIntId<?>> getJavaTypeClass()
    {
        return Objects.requireNonNull(identifierClass, "identifierClass must not be null");
    }

    @Override
    public int extractHashCode(final ObjectBigIntId<?> value)
    {
        return Objects.hashCode(value);
    }

    @Override
    public boolean areEqual(
        final ObjectBigIntId<?> one,
        final ObjectBigIntId<?> another
    )
    {
        return Objects.equals(one, another);
    }

    @Override
    public JdbcType getRecommendedJdbcType(final JdbcTypeIndicators indicators)
    {
        var descriptor = indicators.getTypeConfiguration().getJdbcTypeRegistry().getDescriptor(SqlTypes.BIGINT);
        return descriptor instanceof AdjustableJdbcType
            ? ((AdjustableJdbcType) descriptor).resolveIndicatedType(indicators, this)
            : descriptor;
    }

    @Contract("null, _, _ -> null; !null, _, _ -> !null")
    @Nullable
    @Override
    public <X> X unwrap(
        @Nullable final ObjectBigIntId<?> value,
        @NonNull final Class<X> type,
        @Nullable final WrapperOptions options
    )
    {
        if (value == null) {
            return null;
        }

        return inner.unwrap(value.toLong(), type, options);
    }

    @Contract("null, _ -> null; !null, _ -> !null")
    @Nullable
    @Override
    public <X> ObjectBigIntId<?> wrap(
        @Nullable final X value,
        @Nullable final WrapperOptions options
    )
    {
        if (value == null) {
            return null;
        }

        return wrapBigIntToIdentifier(inner.wrap(value, options));
    }

    @Nullable
    @Override
    public ObjectBigIntId<?> fromString(@Nullable final CharSequence string)
    {
        return (string == null) ? null : wrapBigIntToIdentifier(Long.parseLong(string.toString()));
    }

    @Override
    public MutabilityPlan<ObjectBigIntId<?>> getMutabilityPlan()
    {
        return ImmutableMutabilityPlan.instance();
    }

    private ObjectBigIntId<?> wrapBigIntToIdentifier(final long id)
    {
        return ObjectBigIntIdTypeUtils.wrapBigIntToIdentifier(
            id,
            Objects.requireNonNull(constructor, "constructor was not yet initialized")
        );
    }

    @Override
    public String toString()
    {
        return "object-bigint-id(%s)".formatted(identifierClass != null ? identifierClass.getName() : "???");
    }

    @SuppressWarnings("unused")
    private void writeObject(final ObjectOutputStream stream)
    {
        throw new UnsupportedOperationException("Serialization not supported");
    }

    @SuppressWarnings("unused")
    private void readObject(final ObjectInputStream stream)
    {
        throw new UnsupportedOperationException("Serialization not supported");
    }

}
