package org.framefork.typedIds.bigint.json.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.framefork.typedIds.bigint.ObjectBigIntIdMock;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectBigIntIdJacksonModuleTest
{

    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new ObjectBigIntIdJacksonModule());

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

    record Dto(ObjectBigIntIdMock id)
    {

    }

}
