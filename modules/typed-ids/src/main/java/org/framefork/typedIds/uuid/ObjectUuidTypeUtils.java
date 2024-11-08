package org.framefork.typedIds.uuid;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.UUID;

public final class ObjectUuidTypeUtils
{

    private ObjectUuidTypeUtils()
    {
    }

    @SuppressWarnings("unchecked")
    public static Class<ObjectUuid<?>> readIdentifierClass(
        final String className,
        final String propertyName
    )
    {
        try {
            Class<?> aClass = Class.forName(className);
            Field declaredField = aClass.getDeclaredField(propertyName);
            Class<?> declaredFieldType = declaredField.getType();

            if (!ObjectUuid.class.isAssignableFrom(declaredFieldType)) {
                throw new IllegalArgumentException(String.format("Property %s$%s is expected to be an instance of %s", className, propertyName, ObjectUuid.class));
            }

            return (Class<ObjectUuid<?>>) declaredFieldType;

        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    public static MethodHandle getMainConstructor(final Class<?> type)
    {
        try {
            Constructor<?> constructor = type.getDeclaredConstructor(UUID.class);
            constructor.setAccessible(true);
            return MethodHandles.lookup().unreflectConstructor(constructor);

        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalArgumentException(String.format("Cannot resolve constructor %s(UUID)", type.getName()), e);
        }
    }

    public static ObjectUuid<?> wrapUuidToIdentifier(final UUID uuid, MethodHandle mainConstructor)
    {
        try {
            return getObjectUuidRawClass().cast(mainConstructor.invoke(uuid));

        } catch (RuntimeException e) {
            throw e;

        } catch (Throwable e) {
            String typeName = mainConstructor.type().returnType().getName();
            throw new IllegalArgumentException(String.format("Failed construction of %s using (\"%s\")", typeName, uuid), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Class<ObjectUuid<?>> getObjectUuidRawClass()
    {
        return (Class<ObjectUuid<?>>) (Class<?>) ObjectUuid.class;
    }

}
