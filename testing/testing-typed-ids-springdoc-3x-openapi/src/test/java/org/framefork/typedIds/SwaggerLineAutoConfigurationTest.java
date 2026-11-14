package org.framefork.typedIds;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * springdoc 3.0.x runs on swagger-core 2.2.x, so with both OpenAPI modules on the classpath only typed-ids-openapi-swagger2 may act.
 */
@SpringBootTest(
    classes = {
        Application.class,
    },
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
class SwaggerLineAutoConfigurationTest
{

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @SuppressWarnings("UnnecessarilyFullyQualified")
    public void onlyTheSwagger2ConverterIsRegistered()
    {
        assertThat(applicationContext.getBeansOfType(org.framefork.typedIds.swagger.TypedIdsModelConverter.class)).hasSize(1);
        assertThat(applicationContext.getBeansOfType(org.framefork.typedIds.swagger3.TypedIdsModelConverter.class)).isEmpty();
    }

}
