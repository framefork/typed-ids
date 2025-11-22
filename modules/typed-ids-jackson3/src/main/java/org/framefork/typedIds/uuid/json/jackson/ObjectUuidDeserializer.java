package org.framefork.typedIds.uuid.json.jackson;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import org.framefork.typedIds.common.ReflectionHacks;
import org.framefork.typedIds.uuid.ObjectUuid;
import org.framefork.typedIds.uuid.ObjectUuidTypeUtils;

import java.lang.invoke.MethodHandle;
import java.util.UUID;

public class ObjectUuidDeserializer extends ValueDeserializer<ObjectUuid<?>>
{

    private final MethodHandle constructor;

    public ObjectUuidDeserializer(final Class<?> identifierClass)
    {
        if (!ObjectUuid.class.isAssignableFrom(identifierClass)) {
            throw new IllegalArgumentException("Type %s is not a subtype of %s".formatted(identifierClass, ObjectUuid.class));
        }

        this.constructor = ReflectionHacks.getConstructor(identifierClass, UUID.class);
    }

    @Override
    public ObjectUuid<?> deserialize(
        final JsonParser parser,
        final DeserializationContext context
    )
    {
        var stringUuid = parser.getValueAsString();
        var uuid = UUID.fromString(stringUuid);

        return ObjectUuidTypeUtils.wrapUuidToIdentifier(uuid, constructor);
    }

}
