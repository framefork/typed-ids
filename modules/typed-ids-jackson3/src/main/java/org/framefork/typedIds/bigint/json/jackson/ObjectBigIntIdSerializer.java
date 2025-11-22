package org.framefork.typedIds.bigint.json.jackson;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.SerializationContext;
import org.framefork.typedIds.bigint.ObjectBigIntId;

public class ObjectBigIntIdSerializer extends ValueSerializer<ObjectBigIntId<?>>
{

    @Override
    public void serialize(
        final ObjectBigIntId<?> id,
        final JsonGenerator jsonGenerator,
        final SerializationContext serializationContext
    )
    {
        jsonGenerator.writeNumber(id.toLong());
    }

}
