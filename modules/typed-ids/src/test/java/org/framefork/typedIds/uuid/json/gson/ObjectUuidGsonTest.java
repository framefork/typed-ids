package org.framefork.typedIds.uuid.json.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.framefork.typedIds.uuid.ObjectUuidMock;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectUuidGsonTest
{

    private final Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(new ObjectUuidTypeAdapterFactory())
        .create();

    @Test
    public void functional() throws Exception
    {
        var id33 = ObjectUuidMock.from("33a7641c-811e-40b7-986e-ad109cfcf220");
        var originalDto = new Dto(id33);

        var dtoJson = gson.toJson(originalDto);
        assertThat(dtoJson).isEqualTo("{\"id\":\"33a7641c-811e-40b7-986e-ad109cfcf220\"}");

        var deserializedDto = gson.fromJson(dtoJson, Dto.class);
        assertThat(deserializedDto).isEqualTo(originalDto);
    }

    record Dto(ObjectUuidMock id)
    {

    }

}
