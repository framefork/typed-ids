package org.framefork.typedIds.common;

import org.atteo.classindex.ClassIndex;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

@ApiStatus.Internal
public final class ServiceLoaderUtils
{

    private static final Logger log = LoggerFactory.getLogger(ServiceLoaderUtils.class);

    private ServiceLoaderUtils()
    {
    }

    @Nullable
    public static <T> T getSingleOrNull(final Class<T> type)
    {
        ServiceLoader<T> serviceLoader = ServiceLoader.load(type);
        Iterator<T> iterator = serviceLoader.iterator();

        T first = iterator.hasNext() ? iterator.next() : null;
        if (iterator.hasNext()) {
            log.warn("Multiple registered implementations of {} found, can't decide which one to use, therefore using none of them", type.getName());
            return null;
        }

        return first;
    }

    public static <T> List<Class<? extends T>> getIndexedSubclassesFor(final Class<T> type)
    {
        Iterable<Class<? extends T>> subclasses = ClassIndex.getSubclasses(type);

        List<Class<? extends T>> result = new ArrayList<>();
        subclasses.forEach(result::add);
        result.sort(Comparator.comparing(Class::getName));

        return result;
    }

}
