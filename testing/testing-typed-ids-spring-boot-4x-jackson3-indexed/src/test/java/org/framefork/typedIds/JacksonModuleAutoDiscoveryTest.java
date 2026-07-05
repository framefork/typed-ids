package org.framefork.typedIds;

import org.framefork.typedIds.bigint.SampleBigIntId;
import org.framefork.typedIds.uuid.SampleUuidId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    classes = {
        Application.class,
    }
)
class JacksonModuleAutoDiscoveryTest
{

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void bootManagedMapperRoundtripsTypedIds() throws Exception
    {
        var original = new Dto(
            SampleUuidId.from("33a7641c-811e-40b7-986e-ad109cfcf220"),
            SampleBigIntId.from(42L)
        );

        var json = objectMapper.writeValueAsString(original);
        assertThat(json).isEqualTo("{\"uuidId\":\"33a7641c-811e-40b7-986e-ad109cfcf220\",\"bigIntId\":42}");

        var deserialized = objectMapper.readValue(json, Dto.class);
        assertThat(deserialized).isEqualTo(original);
    }

    record Dto(SampleUuidId uuidId, SampleBigIntId bigIntId)
    {

    }

}
