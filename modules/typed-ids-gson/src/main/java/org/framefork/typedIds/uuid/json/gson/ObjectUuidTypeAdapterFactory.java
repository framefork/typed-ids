package org.framefork.typedIds.uuid.json.gson;

import com.google.auto.service.AutoService;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import org.framefork.typedIds.uuid.ObjectUuid;
import org.framefork.typedIds.uuid.ObjectUuidTypeUtils;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unchecked")
@AutoService(TypeAdapterFactory.class)
public class ObjectUuidTypeAdapterFactory implements TypeAdapterFactory
{

    @Nullable
    @Override
    public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> typeToken)
    {
        var rawType = (Class<T>) typeToken.getRawType();
        if (!ObjectUuidTypeUtils.getObjectUuidRawClass().isAssignableFrom(rawType)) {
            return null;
        }

        return (TypeAdapter<T>) casted(rawType);
    }

    private static <T extends ObjectUuid<T>> ObjectUuidTypeAdapter<?> casted(final Class<?> rawType)
    {
        return new ObjectUuidTypeAdapter<>((Class<T>) rawType);
    }

}
