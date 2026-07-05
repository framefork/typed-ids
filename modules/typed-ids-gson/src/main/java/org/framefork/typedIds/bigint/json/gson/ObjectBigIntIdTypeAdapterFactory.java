package org.framefork.typedIds.bigint.json.gson;

import com.google.auto.service.AutoService;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.ObjectBigIntIdTypeUtils;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unchecked")
@AutoService(TypeAdapterFactory.class)
public class ObjectBigIntIdTypeAdapterFactory implements TypeAdapterFactory
{

    @Nullable
    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> typeToken)
    {
        var rawType = (Class<T>) typeToken.getRawType();
        if (!ObjectBigIntIdTypeUtils.getObjectBigIntIdRawClass().isAssignableFrom(rawType)) {
            return null;
        }

        return (TypeAdapter<T>) casted(rawType);
    }

    private static <T extends ObjectBigIntId<T>> ObjectBigIntIdTypeAdapter<?> casted(final Class<?> rawType)
    {
        return new ObjectBigIntIdTypeAdapter<>((Class<T>) rawType);
    }

}
