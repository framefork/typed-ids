package org.framefork.typedIds.uuid.json.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.framefork.typedIds.common.ReflectionHacks;
import org.framefork.typedIds.uuid.ObjectUuid;
import org.framefork.typedIds.uuid.ObjectUuidTypeUtils;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.util.UUID;

public class ObjectUuidDeserializer extends JsonDeserializer<ObjectUuid<?>>
{

    private final MethodHandle constructor;

    public ObjectUuidDeserializer(final Class<?> identifierClass)
    {
        if (!ObjectUuid.class.isAssignableFrom(identifierClass)) {
            throw new IllegalArgumentException("Type %s is not a subtype of %s".formatted(identifierClass, ObjectUuid.class));
        }

        this.constructor = ReflectionHacks.getMainConstructor(identifierClass, UUID.class);
    }

    @Override
    public ObjectUuid<?> deserialize(
        final JsonParser parser,
        final DeserializationContext context
    ) throws
        IOException
    {
        var stringUuid = parser.getValueAsString();
        var uuid = UUID.fromString(stringUuid);

        return ObjectUuidTypeUtils.wrapUuidToIdentifier(uuid, constructor);
    }

}
