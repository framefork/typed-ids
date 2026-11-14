package org.framefork.typedIds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SuppressWarnings("PrivateConstructorForUtilityClass")
public class Application
{

    public static void main(String[] args)
    {
        // Boot 4 discovers the typed-ids @AutoService JacksonModule via find-and-add-modules (default on), no customizer needed
        SpringApplication.run(Application.class);
    }

}
