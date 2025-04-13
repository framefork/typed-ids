package org.framefork.typedIds;

import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.common.LazyValue;
import org.framefork.typedIds.common.ServiceLoaderUtils;
import org.framefork.typedIds.uuid.ObjectUuid;

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
        ServiceLoaderUtils.getIndexedSubclassesFor(ObjectBigIntId.class));

    private final static LazyValue<List<Class<? extends ObjectUuid>>> objectUuidClasses = new LazyValue<>(() ->
        ServiceLoaderUtils.getIndexedSubclassesFor(ObjectUuid.class));

    public static List<Class<? extends ObjectBigIntId>> getObjectBigIntIdClasses()
    {
        return objectBigIntIdClasses.get();
    }

    public static List<Class<? extends ObjectUuid>> getObjectUuidClasses()
    {
        return objectUuidClasses.get();
    }

}
