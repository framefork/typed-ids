package org.framefork.typedIds.common;

import org.framefork.typedIds.TypedId;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.ObjectBigIntIdTypeUtils;
import org.framefork.typedIds.uuid.ObjectUuid;
import org.framefork.typedIds.uuid.ObjectUuidTypeUtils;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class TypedIdTypeUtils
{

    private TypedIdTypeUtils()
    {
    }

    /**
     * Converts the source to the given typed id class, choosing between {@link ObjectUuidTypeUtils#convert} and {@link ObjectBigIntIdTypeUtils#convert}
     * by the id class; see those for the accepted sources and the exceptions.
     *
     * @throws IllegalArgumentException also when the id class is neither an {@link ObjectUuid} nor an {@link ObjectBigIntId} subtype
     */
    @SuppressWarnings("unchecked")
    public static TypedId<?> convert(final Class<?> idType, final Object source)
    {
        if (ObjectUuid.class.isAssignableFrom(idType)) {
            return ObjectUuidTypeUtils.convert((Class<ObjectUuid<?>>) idType, source);
        }
        if (ObjectBigIntId.class.isAssignableFrom(idType)) {
            return ObjectBigIntIdTypeUtils.convert((Class<ObjectBigIntId<?>>) idType, source);
        }

        throw new IllegalArgumentException("Type %s is neither a subtype of %s nor of %s".formatted(idType.getName(), ObjectUuid.class.getName(), ObjectBigIntId.class.getName()));
    }

}
