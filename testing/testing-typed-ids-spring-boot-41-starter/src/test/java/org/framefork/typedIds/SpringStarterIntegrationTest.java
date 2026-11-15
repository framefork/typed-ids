package org.framefork.typedIds;

import org.framefork.typedIds.bigint.BigIntEntity;
import org.framefork.typedIds.bigint.BigIntEntityRepository;
import org.framefork.typedIds.uuid.UuidEntity;
import org.framefork.typedIds.uuid.UuidEntityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    classes = {
        Application.class,
    },
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
class SpringStarterIntegrationTest
{

    @Autowired
    private UuidEntityRepository uuidRepository;

    @Autowired
    private BigIntEntityRepository bigIntRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void persistenceRoundtrip()
    {
        var uuidEntity = uuidRepository.save(new UuidEntity("uuid-title"));
        var bigIntEntity = bigIntRepository.save(new BigIntEntity("bigint-title"));

        var loadedUuid = uuidRepository.findById(uuidEntity.getId()).orElseThrow();
        assertThat(loadedUuid.getTitle()).isEqualTo("uuid-title");
        assertThat(loadedUuid.getId()).isEqualTo(uuidEntity.getId());

        var loadedBigInt = bigIntRepository.findById(bigIntEntity.getId()).orElseThrow();
        assertThat(loadedBigInt.getTitle()).isEqualTo("bigint-title");
        assertThat(loadedBigInt.getId()).isEqualTo(bigIntEntity.getId());
    }

    @Test
    public void jsonRoundtrip() throws Exception
    {
        var original = new Dto(
            UuidEntity.Id.from("33a7641c-811e-40b7-986e-ad109cfcf220"),
            BigIntEntity.Id.from(42L)
        );

        var json = objectMapper.writeValueAsString(original);
        assertThat(json).isEqualTo("{\"uuidId\":\"33a7641c-811e-40b7-986e-ad109cfcf220\",\"bigIntId\":42}");

        var deserialized = objectMapper.readValue(json, Dto.class);
        assertThat(deserialized).isEqualTo(original);
    }

    record Dto(UuidEntity.Id uuidId, BigIntEntity.Id bigIntId)
    {

    }

}
