package org.framefork.typedIds.bigint.json.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.ObjectBigIntIdTypeUtils;
import org.framefork.typedIds.common.ReflectionHacks;

import java.io.IOException;
import java.lang.invoke.MethodHandle;

public class ObjectBigIntIdTypeAdapter<T extends ObjectBigIntId<T>> extends TypeAdapter<T>
{

    private final MethodHandle constructor;

    public ObjectBigIntIdTypeAdapter(final Class<T> identifierClass)
    {
        if (!ObjectBigIntId.class.isAssignableFrom(identifierClass)) {
            throw new IllegalArgumentException("Type %s is not a subtype of %s".formatted(identifierClass, ObjectBigIntId.class));
        }

        this.constructor = ReflectionHacks.getConstructor(identifierClass, long.class);
    }

    @Override
    public void write(final JsonWriter writer, final T value) throws IOException
    {
        writer.jsonValue(String.valueOf(value.toLong()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public T read(final JsonReader in) throws IOException
    {
        var raw = in.nextLong();
        return (T) ObjectBigIntIdTypeUtils.wrapBigIntToIdentifier(raw, constructor);
    }

}
