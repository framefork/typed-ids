package org.framefork.typedIds.spring.convert.boot;

import org.framefork.typedIds.spring.convert.TypedIdsGenericConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.annotation.Bean;

/**
 * Registers the typed-id conversions as a {@link TypedIdsGenericConverter} bean.
 * <p>
 * Spring Boot adds {@code GenericConverter} beans to the conversion service of Spring MVC and WebFlux, so request parameters and path variables bind
 * to typed ids, and the {@link ConfigurationPropertiesBinding} qualifier adds it to configuration-properties binding.
 * It deliberately references no web auto-configuration class, because those moved packages in Spring Boot 4.
 * <p>
 * Set {@code framefork.typed-ids.spring.converters.enabled=false} to turn it off. Prefer the property to {@code spring.autoconfigure.exclude}:
 * a profile or a test that sets that list again replaces it, and the exclusion gets lost.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "framefork.typed-ids.spring.converters", name = "enabled", havingValue = "true", matchIfMissing = true)
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
