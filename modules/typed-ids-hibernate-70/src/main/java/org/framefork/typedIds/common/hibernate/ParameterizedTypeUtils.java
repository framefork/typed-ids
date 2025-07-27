package org.framefork.typedIds.common.hibernate;

import org.framefork.typedIds.common.ReflectionHacks;
import org.hibernate.usertype.DynamicParameterizedType;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Properties;

@ApiStatus.Internal
@SuppressWarnings("removal")
public final class ParameterizedTypeUtils
{

    private ParameterizedTypeUtils()
    {
    }

    public static Properties forClass(final Class<?> implClass)
    {
        var parameters = new Properties();
        parameters.put(DynamicParameterizedType.PARAMETER_TYPE, new ParameterizedParameterType(implClass));
        return parameters;
    }

    @SuppressWarnings("unchecked")
    public static <ExpectedType> Class<ExpectedType> getReturnedClass(final Properties parameters, final Class<?> expectedType)
    {
        var parameterType = (DynamicParameterizedType.ParameterType) parameters.get(DynamicParameterizedType.PARAMETER_TYPE);
        if (parameterType != null) {
            Class<?> actualType = parameterType.getReturnedClass();
            if (!expectedType.isAssignableFrom(actualType)) {
                throw new IllegalArgumentException("Expected %s but found %s".formatted(expectedType, actualType));
            }
            return (Class<ExpectedType>) actualType;

        } else {
            String entityClass = Objects.requireNonNull(parameters.get(DynamicParameterizedType.ENTITY), "parameters.get(ENTITY) must not be null").toString();
            String propertyName = Objects.requireNonNull(parameters.get(DynamicParameterizedType.PROPERTY), "parameters.get(PROPERTY) must not be null").toString();

            return ReflectionHacks.getFieldTypeChecked(entityClass, propertyName, expectedType);
        }
    }

    public static final class ParameterizedParameterType implements DynamicParameterizedType.ParameterType
    {

        private final Class<?> clazz;

        public ParameterizedParameterType(final Class<?> clazz)
        {
            this.clazz = clazz;
        }

        @Override
        public Class<?> getReturnedClass()
        {
            return clazz;
        }

        @Override
        public Annotation[] getAnnotationsMethod()
        {
            return new Annotation[0];
        }

        @Override
        public String getCatalog()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getSchema()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getTable()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isPrimaryKey()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public String[] getColumns()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Long[] getColumnLengths()
        {
            throw new UnsupportedOperationException();
        }

    }

}
