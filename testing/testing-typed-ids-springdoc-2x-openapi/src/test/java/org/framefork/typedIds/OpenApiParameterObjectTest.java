package org.framefork.typedIds;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * SpringDoc expands a {@code @ParameterObject} into one query parameter per field, and decides by its own simple-type list which fields stay a single
 * parameter. Typed ids must stay a single parameter with the typed-id schema, like {@code page}.
 */
@SpringBootTest(
    classes = {
        Application.class,
    },
    properties = {
        "framefork.typed-ids.openapi.as-ref=false",
        "springdoc.api-docs.path=/api-docs",
        // its own application context: TypedIdsModelConverter.idsAsRef is static, so a context shared with OpenApiSchemaIdsInlineTest
        // would render that test's document with whatever a test class in between set it to
        "spring.application.name=openapi-parameter-object-test",
    },
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
class OpenApiParameterObjectTest
{

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void typedIdFieldsOfAParameterObjectAreQueryParameters() throws Exception
    {
        var openApiFullRaw = mockMvc.perform(get("/api-docs"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        var parameters = objectMapper.readTree(openApiFullRaw).path("paths").path("/api/filter").path("get").path("parameters");
        var names = new ArrayList<String>();
        parameters.forEach(parameter -> names.add(parameter.path("name").asText()));
        assertThat(names).containsExactlyInAnyOrder("uuidId", "bigIntId", "bigIntIds", "page");

        var uuidId = parameter(parameters, "uuidId");
        assertThat(uuidId.path("in").asText()).isEqualTo("query");
        assertThat(uuidId.path("schema").path("type").asText()).isEqualTo("string");
        assertThat(uuidId.path("schema").path("format").asText()).isEqualTo("uuid");

        var bigIntId = parameter(parameters, "bigIntId");
        assertThat(bigIntId.path("in").asText()).isEqualTo("query");
        assertThat(bigIntId.path("schema").path("type").asText()).isEqualTo("integer");
        assertThat(bigIntId.path("schema").path("format").asText()).isEqualTo("int64");

        var bigIntIds = parameter(parameters, "bigIntIds");
        assertThat(bigIntIds.path("in").asText()).isEqualTo("query");
        assertThat(bigIntIds.path("schema").path("type").asText()).isEqualTo("array");
        assertThat(bigIntIds.path("schema").path("items").path("type").asText()).isEqualTo("integer");
        assertThat(bigIntIds.path("schema").path("items").path("format").asText()).isEqualTo("int64");
    }

    private static JsonNode parameter(final JsonNode parameters, final String name)
    {
        List<JsonNode> matching = new ArrayList<>();
        parameters.forEach(parameter -> {
            if (name.equals(parameter.path("name").asText())) {
                matching.add(parameter);
            }
        });
        assertThat(matching).as("parameter %s", name).hasSize(1);
        return matching.get(0);
    }

}
