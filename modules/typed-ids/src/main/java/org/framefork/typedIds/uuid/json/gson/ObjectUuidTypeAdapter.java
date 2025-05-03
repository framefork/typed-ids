package org.framefork.typedIds.uuid.json.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.framefork.typedIds.common.ReflectionHacks;
import org.framefork.typedIds.uuid.ObjectUuid;
import org.framefork.typedIds.uuid.ObjectUuidTypeUtils;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.util.UUID;

public class ObjectUuidTypeAdapter<T extends ObjectUuid<T>> extends TypeAdapter<T>
{

    private final MethodHandle constructor;

    public ObjectUuidTypeAdapter(final Class<T> identifierClass)
    {
        if (!ObjectUuid.class.isAssignableFrom(identifierClass)) {
            throw new IllegalArgumentException("Type %s is not a subtype of %s".formatted(identifierClass, ObjectUuid.class));
        }

        this.constructor = ReflectionHacks.getMainConstructor(identifierClass, UUID.class);
    }

    @Override
    public void write(final JsonWriter writer, final T value) throws IOException
    {
        writer.value(value.toNativeUuid().toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public T read(final JsonReader in) throws IOException
    {
        var uuid = UUID.fromString(in.nextString());
        return (T) ObjectUuidTypeUtils.wrapUuidToIdentifier(uuid, constructor);
    }

}
