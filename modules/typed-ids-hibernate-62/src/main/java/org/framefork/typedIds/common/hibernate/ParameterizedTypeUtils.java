package org.framefork.typedIds.common.hibernate;

import org.hibernate.usertype.DynamicParameterizedType;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.Annotation;
import java.util.Properties;

@ApiStatus.Internal
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
