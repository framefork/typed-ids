package org.framefork.typedIds.springdoc.config;

import io.swagger.v3.core.converter.ModelConverter;
import org.framefork.typedIds.swagger.TypedIdsModelConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Lazy;

@AutoConfiguration
@ConditionalOnClass(ModelConverter.class)
@Conditional(OnSwaggerCore2Condition.class)
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

    /**
     * Registers typed ids as simple types for SpringDoc's {@code @ParameterObject} expansion, when SpringDoc is on the classpath.
     */
    @Bean
    @ConditionalOnClass(name = "org.springdoc.core.utils.SpringDocUtils")
    @Lazy(false) // nothing injects this bean, so under spring.main.lazy-initialization it would never be created and the registration would never run
    static TypedIdsParameterObjectSupport typedIdsSwagger2ParameterObjectSupport()
    {
        return TypedIdsParameterObjectSupport.registerTypedIdsAsSimpleTypes();
    }

}
