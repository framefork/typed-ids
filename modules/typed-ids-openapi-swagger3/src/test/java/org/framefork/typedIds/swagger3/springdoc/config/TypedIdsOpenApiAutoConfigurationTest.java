package org.framefork.typedIds.swagger3.springdoc.config;

import io.swagger.v3.core.util.Json;
import org.framefork.typedIds.swagger3.TypedIdsModelConverter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

final class TypedIdsOpenApiAutoConfigurationTest
{

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(TypedIdsOpenApiAutoConfiguration.class));

    @Test
    void registersTheConverterOnSwaggerCore3()
    {
        contextRunner.run(context -> assertThat(context).hasSingleBean(TypedIdsModelConverter.class));
    }

    @Test
    void skipsTheParameterObjectRegistrationWithoutSpringDoc()
    {
        // this module's test classpath has no SpringDoc, as in an application that uses swagger-core without it
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(TypedIdsParameterObjectSupport.class);
        });
    }

    @Test
    void staysOffWhenSwaggerCoreIsMissing()
    {
        contextRunner
            .withClassLoader(new FilteredClassLoader(Json.class))
            .run(context -> assertThat(context).doesNotHaveBean(TypedIdsModelConverter.class));
    }

}
