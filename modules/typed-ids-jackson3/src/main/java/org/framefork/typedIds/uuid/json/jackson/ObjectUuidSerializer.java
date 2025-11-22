package org.framefork.typedIds.uuid.json.jackson;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import org.framefork.typedIds.uuid.ObjectUuid;

public class ObjectUuidSerializer extends ValueSerializer<ObjectUuid<?>>
{

    @Override
    public void serialize(
        final ObjectUuid<?> uuid,
        final JsonGenerator jsonGenerator,
        final SerializationContext serializationContext
    )
    {
        jsonGenerator.writeString(uuid.toNativeUuid().toString());
    }

}
