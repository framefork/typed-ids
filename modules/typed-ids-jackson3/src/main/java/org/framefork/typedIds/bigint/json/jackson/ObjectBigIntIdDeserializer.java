package org.framefork.typedIds.bigint.json.jackson;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.ObjectBigIntIdTypeUtils;
import org.framefork.typedIds.common.ReflectionHacks;

import java.lang.invoke.MethodHandle;

public class ObjectBigIntIdDeserializer extends ValueDeserializer<ObjectBigIntId<?>>
{

    private final MethodHandle constructor;

    public ObjectBigIntIdDeserializer(final Class<?> identifierClass)
    {
        if (!ObjectBigIntId.class.isAssignableFrom(identifierClass)) {
            throw new IllegalArgumentException("Type %s is not a subtype of %s".formatted(identifierClass, ObjectBigIntId.class));
        }

        this.constructor = ReflectionHacks.getConstructor(identifierClass, long.class);
    }

    @Override
    public ObjectBigIntId<?> deserialize(
        final JsonParser parser,
        final DeserializationContext context
    )
    {
        var longValue = parser.getLongValue();
        return ObjectBigIntIdTypeUtils.wrapBigIntToIdentifier(longValue, constructor);
    }

}
