package org.framefork.typedIds.uuid.hibernate;

import org.framefork.typedIds.uuid.ObjectUuid;
import org.framefork.typedIds.uuid.ObjectUuidTypeUtils;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.Size;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.BasicJavaType;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.java.MutabilityPlan;
import org.hibernate.type.descriptor.java.UUIDJavaType;
import org.hibernate.type.descriptor.jdbc.AdjustableJdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.hibernate.type.descriptor.jdbc.VarbinaryJdbcType;
import org.hibernate.usertype.DynamicParameterizedType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

public class ObjectUuidJavaType implements BasicJavaType<ObjectUuid<?>>, DynamicParameterizedType, Serializable
{

    public static final int UUID_BYTE_LENGTH = 16;

    private final UUIDJavaType inner;

    @Nullable
    private Class<ObjectUuid<?>> identifierClass;
    @Nullable
    private MethodHandle constructor;

    public ObjectUuidJavaType()
    {
        this.inner = UUIDJavaType.INSTANCE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setParameterValues(final Properties parameters)
    {
        @Nullable var parameterType = (ParameterType) parameters.get(PARAMETER_TYPE);
        if (parameterType != null) {
            this.identifierClass = (Class<ObjectUuid<?>>) parameterType.getReturnedClass();

        } else {
            String entityClass = Objects.requireNonNull(parameters.get(ENTITY), "parameters.get(ENTITY) must not be null").toString();
            String propertyName = Objects.requireNonNull(parameters.get(PROPERTY), "parameters.get(PROPERTY) must not be null").toString();

            this.identifierClass = ObjectUuidTypeUtils.readIdentifierClass(entityClass, propertyName);
        }

        if (!ObjectUuid.class.isAssignableFrom(identifierClass)) {
            throw new IllegalArgumentException("Type %s is not a subtype of %s".formatted(identifierClass, ObjectUuid.class));
        }

        this.constructor = ObjectUuidTypeUtils.getMainConstructor(identifierClass);
    }

    @Override
    public Type getJavaType()
    {
        return getJavaTypeClass();
    }

    @Override
    public Class<ObjectUuid<?>> getJavaTypeClass()
    {
        return Objects.requireNonNull(identifierClass, "identifierClass must not be null");
    }

    @Override
    public int extractHashCode(final ObjectUuid<?> value)
    {
        return Objects.hashCode(value);
    }

    @Override
    public boolean areEqual(
        final ObjectUuid<?> one,
        final ObjectUuid<?> another
    )
    {
        return Objects.equals(one, another);
    }

    @Override
    public JdbcType getRecommendedJdbcType(final JdbcTypeIndicators indicators)
    {
        final JdbcType descriptor = indicators.getJdbcType(indicators.resolveJdbcTypeCode(SqlTypes.UUID));
        return descriptor instanceof AdjustableJdbcType
            ? ((AdjustableJdbcType) descriptor).resolveIndicatedType(indicators, this)
            : descriptor;
    }

    @Override
    public long getDefaultSqlLength(final Dialect dialect, final JdbcType jdbcType)
    {
        if (jdbcType instanceof VarbinaryJdbcType) {
            return UUID_BYTE_LENGTH;
        }

        return Size.DEFAULT_LENGTH;
    }

    @Nullable
    @Override
    public <X> X unwrap(
        @Nullable final ObjectUuid<?> value,
        @NotNull final Class<X> type,
        @NotNull final WrapperOptions options
    )
    {
        if (value == null) {
            return null;
        }

        return inner.unwrap(value.toNativeUuid(), type, options);
    }

    @Nullable
    @Override
    public <X> ObjectUuid<?> wrap(
        @Nullable final X value,
        @NotNull final WrapperOptions options
    )
    {
        if (value == null) {
            return null;
        }

        return wrapUuidToIdentifier(inner.wrap(value, options));
    }

    @Nullable
    @Override
    public ObjectUuid<?> fromString(@Nullable final CharSequence string)
    {
        return (string == null) ? null : wrapUuidToIdentifier(UUID.fromString(string.toString()));
    }

    @Override
    public MutabilityPlan<ObjectUuid<?>> getMutabilityPlan()
    {
        return ImmutableMutabilityPlan.instance();
    }

    private ObjectUuid<?> wrapUuidToIdentifier(final UUID uuid)
    {
        return ObjectUuidTypeUtils.wrapUuidToIdentifier(
            uuid,
            Objects.requireNonNull(constructor, "constructor was not yet initialized")
        );
    }

    @Override
    public String toString()
    {
        return "object-uuid(%s)".formatted(identifierClass != null ? identifierClass.getName() : "???");
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
