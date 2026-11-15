package org.framefork.typedIds.swagger3.springdoc.config;

import org.framefork.typedIds.TypedId;
import org.springdoc.core.utils.SpringDocUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Makes SpringDoc treat typed ids as simple types when it expands a {@code @ParameterObject} into query parameters.
 * <p>
 * SpringDoc decides which fields of a parameter object stay a single parameter by its own simple-type predicates and never asks the {@code ModelConverter}s,
 * so without this a typed id field is taken for a nested object and silently dropped from the spec. The predicates live in a static list in SpringDoc,
 * so they are registered once per JVM, not once per application context.
 */
final class TypedIdsParameterObjectSupport
{

    private static final AtomicBoolean REGISTERED = new AtomicBoolean();

    private TypedIdsParameterObjectSupport()
    {
    }

    /**
     * Registers the predicate, unless an earlier application context in this JVM already did, and returns a marker bean.
     */
    static TypedIdsParameterObjectSupport registerTypedIdsAsSimpleTypes()
    {
        if (REGISTERED.compareAndSet(false, true)) {
            SpringDocUtils.getConfig().addSimpleTypePredicateForParameterObject(TypedId.class::isAssignableFrom);
        }
        return new TypedIdsParameterObjectSupport();
    }

}
