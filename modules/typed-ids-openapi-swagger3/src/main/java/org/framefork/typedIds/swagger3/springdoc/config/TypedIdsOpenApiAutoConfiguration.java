package org.framefork.typedIds.swagger3.springdoc.config;

import io.swagger.v3.core.converter.ModelConverter;
import org.framefork.typedIds.swagger3.TypedIdsModelConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

@AutoConfiguration
@ConditionalOnClass(ModelConverter.class)
@Conditional(OnSwaggerCore3Condition.class)
@EnableConfigurationProperties(TypedIdsOpenApiProperties.class)
public class TypedIdsOpenApiAutoConfiguration
{

    private final TypedIdsOpenApiProperties properties;

    public TypedIdsOpenApiAutoConfiguration(final TypedIdsOpenApiProperties properties)
    {
        this.properties = properties;
    }

    @Bean
    public TypedIdsModelConverter typedIdsModelConverter()
    {
        TypedIdsModelConverter.idsAsRef = this.properties.getAsRef();
        return new TypedIdsModelConverter();
    }

}
