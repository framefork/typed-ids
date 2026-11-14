package org.framefork.typedIds.swagger3.springdoc.config;

import org.framefork.typedIds.swagger3.SwaggerCoreLine;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Matches when the application's swagger-core is the 3.x line, see {@link SwaggerCoreLine}.
 */
final class OnSwaggerCore3Condition extends SpringBootCondition
{

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context, final AnnotatedTypeMetadata metadata)
    {
        var message = ConditionMessage.forCondition("swagger-core 3.x");
        if (SwaggerCoreLine.isSwaggerCore3(context.getClassLoader())) {
            return ConditionOutcome.match(message.because("io.swagger.v3.core.util.Json is built on Jackson 3"));
        }

        return ConditionOutcome.noMatch(message.because("io.swagger.v3.core.util.Json is missing or not built on Jackson 3"));
    }

}
