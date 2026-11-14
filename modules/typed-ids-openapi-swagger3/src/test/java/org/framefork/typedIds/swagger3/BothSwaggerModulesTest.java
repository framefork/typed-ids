package org.framefork.typedIds.swagger3;

import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverterContextImpl;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.Schema;
import org.framefork.typedIds.swagger3.uuid.UuidAppGeneratedExplicitMappingEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A Boot 4 starter puts typed-ids-openapi-swagger2 and typed-ids-openapi-swagger3 on one classpath, and here the swagger-core is 3.x.
 */
@SuppressWarnings({"rawtypes", "UnnecessarilyFullyQualified"})
final class BothSwaggerModulesTest
{

    @Test
    void serviceLoaderRegistersBothConvertersButOnlyThisLinesConverterActs()
    {
        var converters = ModelConverters.getInstance().getConverters();
        assertThat(converters).anyMatch(TypedIdsModelConverter.class::isInstance);
        assertThat(converters).anyMatch(org.framefork.typedIds.swagger.TypedIdsModelConverter.class::isInstance);

        var swagger2Converter = new org.framefork.typedIds.swagger.TypedIdsModelConverter();
        var context = new ModelConverterContextImpl(List.of(swagger2Converter));
        var resolved = swagger2Converter.resolve(new AnnotatedType(UuidAppGeneratedExplicitMappingEntity.Id.class), context, Collections.emptyIterator());
        assertThat(resolved).isNull();
    }

    @Test
    void schemaComesFromThisLinesConverterWhileBothAreRegistered()
    {
        var schemas = ModelConverters.getInstance().readAll(UuidAppGeneratedExplicitMappingEntity.class);

        Schema idSchema = schemas.get(UuidAppGeneratedExplicitMappingEntity.class.getSimpleName() + "_Id");
        assertThat(idSchema).isNotNull();
        assertThat(idSchema.getType()).isEqualTo("string");
        assertThat(idSchema.getFormat()).isEqualTo("uuid");
    }

    @Test
    void onlyThisLinesAutoConfigurationActivates()
    {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                org.framefork.typedIds.swagger3.springdoc.config.TypedIdsOpenApiAutoConfiguration.class,
                org.framefork.typedIds.springdoc.config.TypedIdsOpenApiAutoConfiguration.class
            ))
            .run(context -> {
                assertThat(context).hasSingleBean(TypedIdsModelConverter.class);
                assertThat(context).doesNotHaveBean(org.framefork.typedIds.swagger.TypedIdsModelConverter.class);
            });
    }

}
