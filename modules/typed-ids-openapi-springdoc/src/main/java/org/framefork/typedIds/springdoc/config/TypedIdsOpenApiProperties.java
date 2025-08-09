package org.framefork.typedIds.springdoc.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = TypedIdsOpenApiProperties.PREFIX)
public class TypedIdsOpenApiProperties
{

    public static final String PREFIX = "framefork.typed-ids.openapi";

    @NotNull
    private Boolean asRef = true;

    public Boolean getAsRef()
    {
        return asRef;
    }

    public void setAsRef(final Boolean asRef)
    {
        this.asRef = asRef;
    }

}
