package org.framefork.typedIds.uuid.json.jackson3;

import org.framefork.typedIds.uuid.ObjectUuid;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class ObjectUuidSerializer extends ValueSerializer<ObjectUuid<?>>
{

    @Override
    public void serialize(
        final ObjectUuid<?> uuid,
        final JsonGenerator jsonGenerator,
        final SerializationContext context
    )
    {
        jsonGenerator.writeString(uuid.toNativeUuid().toString());
    }

}
