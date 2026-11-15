package org.framefork.typedIds;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Under lazy initialization a bean that nothing injects is never created, so the typed-ids registration for {@code @ParameterObject} must opt out of it.
 * SpringDoc keeps the registration in a static list shared by every context in this JVM, so the document alone cannot prove it for this context;
 * the test also checks that the registering bean was created eagerly.
 */
@SpringBootTest(
    classes = {
        Application.class,
    },
    properties = {
        "spring.main.lazy-initialization=true",
        "springdoc.api-docs.path=/api-docs",
        "spring.application.name=openapi-parameter-object-lazy-init-test",
    },
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
@AutoConfigureMockMvc
class OpenApiParameterObjectLazyInitializationTest
{

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void registersTypedIdsAsSimpleTypesUnderLazyInitialization() throws Exception
    {
        assertThat(applicationContext.getBeanFactory().containsSingleton("typedIdsSwagger2ParameterObjectSupport"))
            .as("the registering bean is created although the context is lazy")
            .isTrue();

        var openApiFullRaw = mockMvc.perform(get("/api-docs"))
            .andReturn()
            .getResponse()
            .getContentAsString();
        var names = new ArrayList<String>();
        objectMapper.readTree(openApiFullRaw).path("paths").path("/api/filter").path("get").path("parameters")
            .forEach(parameter -> names.add(parameter.path("name").asText()));
        assertThat(names).contains("uuidId", "bigIntId", "bigIntIds");
    }

}
