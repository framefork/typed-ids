package org.framefork.typedIds.bigint;

import org.jetbrains.annotations.ApiStatus;

import java.lang.invoke.MethodHandle;

@ApiStatus.Internal
public final class ObjectBigIntIdTypeUtils
{

    private ObjectBigIntIdTypeUtils()
    {
    }

    public static ObjectBigIntId<?> wrapBigIntToIdentifier(final long id, MethodHandle mainConstructor)
    {
        try {
            return getObjectBigIntIdRawClass().cast(mainConstructor.invoke(id));

        } catch (RuntimeException e) {
            throw e;

        } catch (Throwable e) {
            String typeName = mainConstructor.type().returnType().getName();
            throw new IllegalArgumentException(String.format("Failed construction of %s using (\"%s\")", typeName, id), e);
        }
    }

    @SuppressWarnings("unchecked")
    public static Class<ObjectBigIntId<?>> getObjectBigIntIdRawClass()
    {
        return (Class<ObjectBigIntId<?>>) (Class<?>) ObjectBigIntId.class;
    }

}
