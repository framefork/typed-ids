package org.framefork.typedIds.uuid.hibernate;

import io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayType;
import io.hypersistence.utils.hibernate.type.array.internal.AbstractArrayTypeDescriptor;
import io.hypersistence.utils.hibernate.type.array.internal.ArrayUtil;
import io.hypersistence.utils.hibernate.type.util.ParameterizedParameterType;
import org.framefork.typedIds.uuid.ObjectUuid;
import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.usertype.DynamicParameterizedType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

public class ObjectUuidArrayType<T extends ObjectUuid<T>> extends AbstractArrayType<T[]>
{

    @SuppressWarnings("this-escape")
    public ObjectUuidArrayType(
        final Class<T[]> arrayClass,
        final ObjectUuidType objectUuidType
    )
    {
        super(
            new ObjectUUIDArrayTypeDescriptor<>(arrayClass, objectUuidType)
        );
        var parameters = new Properties();
        parameters.put(DynamicParameterizedType.PARAMETER_TYPE, new ParameterizedParameterType(arrayClass));
        parameters.put(SQL_ARRAY_TYPE, getJavaTypeDescriptor().getSqlArrayType());
        setParameterValues(parameters);
    }

    public String[] getRegistrationKeys()
    {
        return new String[]{
            returnedClass().getName(),
            returnedClass().getTypeName(),
            returnedClass().getCanonicalName(),
        };
    }

    @Override
    public ObjectUUIDArrayTypeDescriptor<T> getJavaTypeDescriptor()
    {
        return (ObjectUUIDArrayTypeDescriptor<T>) super.getJavaTypeDescriptor();
    }

    public static <R extends ObjectUuid<R>> ObjectUuidArrayType<R> create(
        final Class<R> elementClass,
        final ObjectUuidType objectUuidType
    )
    {
        return new ObjectUuidArrayType<>(
            ArrayUtil.toArrayClass(elementClass),
            objectUuidType
        );
    }

    @SuppressWarnings("unchecked")
    public static final class ObjectUUIDArrayTypeDescriptor<T extends ObjectUuid<T>> extends AbstractArrayTypeDescriptor<T[]>
    {

        private final ObjectUuidJavaType objectUuidJavaType;

        public ObjectUUIDArrayTypeDescriptor(
            final Class<T[]> arrayObjectClass,
            final ObjectUuidType objectUuidType
        )
        {
            super(arrayObjectClass);
            this.objectUuidJavaType = objectUuidType.getExpressibleJavaType();
        }

        @Override
        protected String getSqlArrayType()
        {
            return "uuid";
        }

        @Override
        public <X> X unwrap(
            final T[] value,
            @NonNull final Class<X> type,
            @Nullable final WrapperOptions options
        )
        {
            if (value.length > 0) {
                @SuppressWarnings("unchecked")
                var result = (X) Arrays.stream(value)
                    .map(item -> objectUuidJavaType.unwrap(item, UUID.class, options))
                    .toArray(UUID[]::new);

                return result;
            }

            return super.unwrap(value, type, options);
        }

        @Override
        public <X> T[] wrap(
            final X value,
            @Nullable final WrapperOptions options
        )
        {
            if (value instanceof Array array) {
                try {
                    var uuidsArray = ArrayUtil.unwrapArray((Object[]) array.getArray(), Object[].class);

                    var objectUuidsArray = Arrays.stream(uuidsArray)
                        .map(item -> objectUuidJavaType.wrap(item, options))
                        .toArray(Object[]::new);

                    return ArrayUtil.unwrapArray(objectUuidsArray, getJavaTypeClass());

                } catch (SQLException e) {
                    throw new HibernateException(new IllegalArgumentException(e));
                }
            }

            return super.wrap(value, options);
        }

    }

}
