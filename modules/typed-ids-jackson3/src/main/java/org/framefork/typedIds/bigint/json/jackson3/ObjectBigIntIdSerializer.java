package org.framefork.typedIds.bigint.json.jackson3;

import org.framefork.typedIds.bigint.ObjectBigIntId;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class ObjectBigIntIdSerializer extends ValueSerializer<ObjectBigIntId<?>>
{

    @Override
    public void serialize(
        final ObjectBigIntId<?> id,
        final JsonGenerator jsonGenerator,
        final SerializationContext context
    )
    {
        jsonGenerator.writeNumber(id.toLong());
    }

}
