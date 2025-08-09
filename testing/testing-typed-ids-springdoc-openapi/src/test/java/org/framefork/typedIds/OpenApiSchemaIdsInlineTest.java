package org.framefork.typedIds;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.DeserializationModule;
import io.swagger.v3.oas.models.media.Schema;
import org.framefork.typedIds.bigint.BigIntAppGeneratedExplicitMappingEntity;
import org.framefork.typedIds.bigint.BigIntAppGeneratedExplicitMappingEntityId;
import org.framefork.typedIds.uuid.UuidAppGeneratedExplicitMappingEntity;
import org.framefork.typedIds.uuid.UuidAppGeneratedExplicitMappingEntityId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    classes = {
        Application.class,
    },
    properties = {
        "framefork.typed-ids.openapi.as-ref=false",
        "springdoc.api-docs.path=/api-docs",
    },
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
class OpenApiSchemaIdsInlineTest
{

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void verifySchema() throws Exception
    {
        var response = restTemplate.getForEntity("/api-docs", String.class);
        assertThat(response.getStatusCode().is2xxSuccessful())
            .as("OpenAPI endpoint should return 2xx")
            .isTrue();

        var openApiFullRaw = response.getBody();
        assertThat(openApiFullRaw).as("OpenAPI document body").isNotBlank();

        var schemasNode = objectMapper.readTree(openApiFullRaw).path("components").path("schemas");

        assertThat(schemasNode.path(UuidAppGeneratedExplicitMappingEntityId.class.getSimpleName()).isMissingNode()).isTrue();
        assertThat(schemasNode.path(BigIntAppGeneratedExplicitMappingEntityId.class.getSimpleName()).isMissingNode()).isTrue();

        var uuidWrapperSchema = objectMapper.convertValue(schemasNode.path(UuidAppGeneratedExplicitMappingEntity.class.getSimpleName()), Schema.class);
        var bigintWrapperSchema = objectMapper.convertValue(schemasNode.path(BigIntAppGeneratedExplicitMappingEntity.class.getSimpleName()), Schema.class);

        var uuidIdSchema = Objects.requireNonNull(uuidWrapperSchema.getProperties().get("id"), "uuidIdSchema must not be null");
        assertThat(uuidIdSchema)
            .isNotNull()
            .isInstanceOfAny(Schema.class)
            .hasFieldOrPropertyWithValue("type", "string")
            .hasFieldOrPropertyWithValue("format", "uuid");

        var bigintIdSchema = Objects.requireNonNull(bigintWrapperSchema.getProperties().get("id"), "bigintIdSchema must not be null");
        assertThat(bigintIdSchema)
            .isNotNull()
            .isInstanceOfAny(Schema.class)
            .hasFieldOrPropertyWithValue("type", "integer")
            .hasFieldOrPropertyWithValue("format", "int64");
    }

    @TestConfiguration
    public static class Config
    {

        @Bean
        public DeserializationModule swaggerDeserializationModule()
        {
            return new DeserializationModule();
        }

    }

}
