package org.framefork.typedIds.uuid;

import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;
import java.util.UUID;

@ApiStatus.Internal
public final class ObjectUuidTypeUtils
{

    private ObjectUuidTypeUtils()
    {
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
