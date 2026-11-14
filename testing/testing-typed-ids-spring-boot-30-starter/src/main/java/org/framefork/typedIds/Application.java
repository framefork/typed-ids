package org.framefork.typedIds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@SuppressWarnings("PrivateConstructorForUtilityClass")
public class Application
{

    public static void main(String[] args)
    {
        SpringApplication.run(Application.class);
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer enableServiceLoaderModules()
    {
        // Boot 3.x does not ServiceLoader-scan Jackson modules by default; the typed-ids @AutoService module needs this to be picked up
        return builder -> builder.findModulesViaServiceLoader(true);
    }

}
