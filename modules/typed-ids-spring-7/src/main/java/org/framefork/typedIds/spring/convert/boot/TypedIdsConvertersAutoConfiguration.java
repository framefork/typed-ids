package org.framefork.typedIds.spring.convert.boot;

import org.framefork.typedIds.spring.convert.TypedIdsGenericConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Bean;

/**
 * Registers the typed-id conversions as a {@link TypedIdsGenericConverter} bean.
 * <p>
 * Spring Boot adds {@code GenericConverter} beans to the conversion service of Spring MVC and WebFlux, so request parameters and path variables bind
 * to typed ids, and the {@link ConfigurationPropertiesBinding} qualifier adds it to configuration-properties binding.
 * It deliberately references no web auto-configuration class, because those moved packages in Spring Boot 4.
 */
@AutoConfiguration
public class TypedIdsConvertersAutoConfiguration
{

    @Bean
    @ConfigurationPropertiesBinding
    @ConditionalOnMissingBean
    public static TypedIdsGenericConverter typedIdsGenericConverter()
    {
        return new TypedIdsGenericConverter();
    }

}
