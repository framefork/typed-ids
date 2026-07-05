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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest(
    classes = {
        Application.class,
    },
    properties = {
        "framefork.typed-ids.openapi.as-ref=true",
        "springdoc.api-docs.path=/api-docs",
    },
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
class OpenApiSchemaIdsAsRefsTest
{

    // swagger-core models are Jackson-2; parse the OpenAPI document with a dedicated Jackson-2 mapper
    // rather than the Boot-managed Jackson-3 ObjectMapper
    private final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new DeserializationModule());

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void verifySchema() throws Exception
    {
        var openApiFullRaw = mockMvc.perform(get("/api-docs"))
            .andReturn()
            .getResponse()
            .getContentAsString();
        assertThat(openApiFullRaw).as("OpenAPI document body").isNotBlank();

        var schemasNode = objectMapper.readTree(openApiFullRaw).path("components").path("schemas");

        var uuidWrapperSchema = objectMapper.convertValue(schemasNode.path(UuidAppGeneratedExplicitMappingEntity.class.getSimpleName()), Schema.class);
        var bigintWrapperSchema = objectMapper.convertValue(schemasNode.path(BigIntAppGeneratedExplicitMappingEntity.class.getSimpleName()), Schema.class);

        assertThat(uuidWrapperSchema)
            .extracting(s -> s.getProperties().get("id"))
            .isInstanceOfAny(Schema.class)
            .hasFieldOrPropertyWithValue("$ref", "#/components/schemas/" + UuidAppGeneratedExplicitMappingEntityId.class.getSimpleName());
        assertThat(bigintWrapperSchema)
            .extracting(s -> s.getProperties().get("id"))
            .isInstanceOfAny(Schema.class)
            .hasFieldOrPropertyWithValue("$ref", "#/components/schemas/" + BigIntAppGeneratedExplicitMappingEntityId.class.getSimpleName());

        var uuidIdSchema = objectMapper.convertValue(schemasNode.path(UuidAppGeneratedExplicitMappingEntityId.class.getSimpleName()), Schema.class);
        var bigintIdSchema = objectMapper.convertValue(schemasNode.path(BigIntAppGeneratedExplicitMappingEntityId.class.getSimpleName()), Schema.class);

        assertThat(uuidIdSchema)
            .isNotNull()
            .isInstanceOfAny(Schema.class)
            .hasFieldOrPropertyWithValue("type", "string")
            .hasFieldOrPropertyWithValue("format", "uuid");
        assertThat(bigintIdSchema)
            .isNotNull()
            .isInstanceOfAny(Schema.class)
            .hasFieldOrPropertyWithValue("type", "integer")
            .hasFieldOrPropertyWithValue("format", "int64");
    }

}
