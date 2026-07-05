package org.framefork.typedIds.bigint.json.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.framefork.typedIds.bigint.ObjectBigIntId;

import java.io.IOException;

public class ObjectBigIntIdSerializer extends JsonSerializer<ObjectBigIntId<?>>
{

    @Override
    public void serialize(
        final ObjectBigIntId<?> id,
        final JsonGenerator jsonGenerator,
        final SerializerProvider serializerProvider
    ) throws
        IOException
    {
        jsonGenerator.writeNumber(id.toLong());
    }

}
