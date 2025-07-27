package org.framefork.typedIds.common;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

@ApiStatus.Internal
public final class ReflectionHacks
{

    private ReflectionHacks()
    {
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> Class<T> classForName(
        @Language("jvm-class-name") final String className
    )
    {
        try {
            return (Class<T>) Class.forName(className, false, ReflectionHacks.class.getClassLoader());
        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
            return null;
        }
    }

    public static boolean classExists(
        @Language("jvm-class-name") final String className
    )
    {
        return classForName(className) != null;
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

    public static MethodHandle getConstructor(final Class<?> type, final Class<?>... parameterTypes)
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

    public static Class<?>[] getAllInterfaces(final Object object)
    {
        var interfaces = new HashSet<Class<?>>();
        Class<?> classNode = object.getClass();
        while (classNode != null && classNode != Object.class) {
            Collections.addAll(interfaces, classNode.getInterfaces());
            classNode = classNode.getSuperclass();
        }
        return interfaces.toArray(Class[]::new);
    }

}
