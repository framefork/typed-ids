package org.framefork.typedIds.bigint.json.jackson3;

import org.framefork.typedIds.bigint.ObjectBigIntIdMock;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectBigIntIdJacksonModuleTest
{

    private final ObjectMapper objectMapper = JsonMapper.builder()
        .addModule(new ObjectBigIntIdJacksonModule())
        .build();

    @Test
    public void functional() throws Exception
    {
        var id42 = ObjectBigIntIdMock.from("42");
        var originalDto = new Dto(id42);

        var dtoJson = objectMapper.writeValueAsString(originalDto);
        assertThat(dtoJson).isEqualTo("{\"id\":42}");

        var deserializedDto = objectMapper.readValue(dtoJson, Dto.class);
        assertThat(deserializedDto).isEqualTo(originalDto);
    }

    @Test
    public void bigIntDeserializationRoundTrip() throws Exception
    {
        var original = ObjectBigIntIdMock.from(9_223_372_036_854_775_807L);

        var json = objectMapper.writeValueAsString(original);
        assertThat(json).isEqualTo("9223372036854775807");

        var deserialized = objectMapper.readValue(json, ObjectBigIntIdMock.class);
        assertThat(deserialized).isEqualTo(original);
    }

    record Dto(ObjectBigIntIdMock id)
    {

    }

}
