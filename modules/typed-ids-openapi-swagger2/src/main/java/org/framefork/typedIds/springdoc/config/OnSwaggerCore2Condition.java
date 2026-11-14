package org.framefork.typedIds.springdoc.config;

import org.framefork.typedIds.swagger.SwaggerCoreLine;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Matches when the application's swagger-core is the 2.x line, see {@link SwaggerCoreLine}.
 */
final class OnSwaggerCore2Condition extends SpringBootCondition
{

    @Override
    public ConditionOutcome getMatchOutcome(final ConditionContext context, final AnnotatedTypeMetadata metadata)
    {
        var message = ConditionMessage.forCondition("swagger-core 2.x");
        if (SwaggerCoreLine.isSwaggerCore2(context.getClassLoader())) {
            return ConditionOutcome.match(message.because("io.swagger.v3.core.util.Json is built on Jackson 2"));
        }

        return ConditionOutcome.noMatch(message.because("io.swagger.v3.core.util.Json is missing or not built on Jackson 2"));
    }

}
