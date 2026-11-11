package org.framefork.typedIds;

import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.common.ClassIndexLoaderUtils;
import org.framefork.typedIds.common.LazyValue;
import org.framefork.typedIds.uuid.ObjectUuid;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This only works when the subtypes of the TypedIds are indexed in compile time.
 * <p>
 * Investigate the <code>typed-ids-index-java-classes-processor</code> module of this library.
 */
@SuppressWarnings("rawtypes")
public final class TypedIdsRegistry
{

    private TypedIdsRegistry()
    {
    }

    private final static LazyValue<List<Class<? extends ObjectBigIntId>>> objectBigIntIdClasses = new LazyValue<>(() ->
        loadIndex(ObjectBigIntId.class));

    private final static LazyValue<List<Class<? extends ObjectUuid>>> objectUuidClasses = new LazyValue<>(() ->
        loadIndex(ObjectUuid.class));

    public static List<Class<? extends ObjectBigIntId>> getObjectBigIntIdClasses()
    {
        return objectBigIntIdClasses.get();
    }

    public static List<Class<? extends ObjectUuid>> getObjectUuidClasses()
    {
        return objectUuidClasses.get();
    }

    private static <T> List<Class<? extends T>> loadIndex(final Class<T> baseType)
    {
        var classes = ClassIndexLoaderUtils.getIndexedSubclassesFor(baseType);

        // The logger is looked up here and not in a static field, so that the registry class itself does not need slf4j to initialize.
        LoggerFactory.getLogger(TypedIdsRegistry.class).debug("Loaded the typed-ids index of {}: {} classes found", baseType.getName(), classes.size());

        return classes;
    }

}
