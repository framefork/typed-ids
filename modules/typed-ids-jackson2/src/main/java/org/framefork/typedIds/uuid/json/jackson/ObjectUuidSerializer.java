package org.framefork.typedIds.uuid.json.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.framefork.typedIds.uuid.ObjectUuid;

import java.io.IOException;

public class ObjectUuidSerializer extends JsonSerializer<ObjectUuid<?>>
{

    @Override
    public void serialize(
        final ObjectUuid<?> uuid,
        final JsonGenerator jsonGenerator,
        final SerializerProvider serializerProvider
    ) throws
        IOException
    {
        jsonGenerator.writeString(uuid.toNativeUuid().toString());
    }

}
