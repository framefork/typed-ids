package org.framefork.typedIds.uuid.json.jackson3;

import org.framefork.typedIds.uuid.ObjectUuidMock;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ObjectUuidJacksonModuleTest
{

    private final ObjectMapper objectMapper = JsonMapper.builder()
        .addModule(new ObjectUuidJacksonModule())
        .build();

    @Test
    public void functional() throws Exception
    {
        var id33 = ObjectUuidMock.from("33a7641c-811e-40b7-986e-ad109cfcf220");
        var originalDto = new Dto(id33);

        var dtoJson = objectMapper.writeValueAsString(originalDto);
        assertThat(dtoJson).isEqualTo("{\"id\":\"33a7641c-811e-40b7-986e-ad109cfcf220\"}");

        var deserializedDto = objectMapper.readValue(dtoJson, Dto.class);
        assertThat(deserializedDto).isEqualTo(originalDto);
    }

    record Dto(ObjectUuidMock id)
    {

    }

}
