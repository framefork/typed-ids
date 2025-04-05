package org.framefork.typedIds.common;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;

@ApiStatus.Internal
public final class ReflectionHacks
{

    private ReflectionHacks()
    {
    }

    public static boolean classExists(
        @Language("jvm-class-name") final String className
    )
    {
        try {
            Class.forName(className, false, ReflectionHacks.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static Class<?> getFieldType(
        @Language("jvm-class-name") final String className,
        final String propertyName
    )
    {
        try {
            Class<?> aClass = Class.forName(className);
            Field declaredField = aClass.getDeclaredField(propertyName);
            return declaredField.getType();

        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <ExpectedType> Class<ExpectedType> getFieldTypeChecked(
        @Language("jvm-class-name") final String className,
        final String propertyName,
        final Class<?> expectedType
    )
    {
        Class<?> declaredFieldType = getFieldType(className, propertyName);

        if (!expectedType.isAssignableFrom(declaredFieldType)) {
            throw new IllegalArgumentException(String.format("Property %s$%s is expected to be an instance of %s", className, propertyName, expectedType));
        }

        return (Class<ExpectedType>) declaredFieldType;
    }

    public static MethodHandle getMainConstructor(final Class<?> type, final Class<?>... parameterTypes)
    {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return MethodHandles.lookup().unreflectConstructor(constructor);

        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException(
                String.format(
                    "Cannot resolve constructor %s(%s)",
                    type.getName(),
                    String.join(",", Arrays.stream(parameterTypes).map(Class::getName).toList())
                ),
                e
            );
        }
    }

}
