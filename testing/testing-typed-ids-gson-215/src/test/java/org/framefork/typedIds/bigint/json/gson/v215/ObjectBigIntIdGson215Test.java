package org.framefork.typedIds.bigint.json.gson.v215;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.framefork.typedIds.bigint.ObjectBigIntIdMock;
import org.framefork.typedIds.bigint.json.gson.ObjectBigIntIdTypeAdapterFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectBigIntIdGson215Test
{

    private final Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(new ObjectBigIntIdTypeAdapterFactory())
        .create();

    @Test
    public void functional() throws Exception
    {
        var id42 = ObjectBigIntIdMock.from("42");
        var originalDto = new Dto(id42);

        var dtoJson = gson.toJson(originalDto);
        assertThat(dtoJson).isEqualTo("{\"id\":42}");

        var deserializedDto = gson.fromJson(dtoJson, Dto.class);
        assertThat(deserializedDto).isEqualTo(originalDto);
    }

    record Dto(ObjectBigIntIdMock id)
    {

    }

}
