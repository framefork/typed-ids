package org.framefork.typedIds.common;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;

@ApiStatus.Internal
public final class ServiceLoaderUtils
{

    private static final String SUBCLASS_INDEX_PREFIX = "META-INF/services/";

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

    @SuppressWarnings("unchecked")
    public static <T> List<Class<? extends T>> getIndexedSubclassesFor(final Class<T> superClass)
    {
        Collection<String> classNames = readIndexFile(SUBCLASS_INDEX_PREFIX + superClass.getCanonicalName());

        var classes = classNames.stream()
            .map(ReflectionHacks::classForName)
            .filter(Objects::nonNull)
            .map(klass -> superClass.isAssignableFrom(klass) ? (Class<? extends T>) klass : null)
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(Class::getName))
            .toList();

        return (List<Class<? extends T>>) (List<?>) classes;
    }

    private static Collection<String> readIndexFile(final String resourceFile)
    {
        try {
            Set<String> entries = new HashSet<>();

            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(resourceFile);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();

                try (var reader = new BufferedReader(new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8))) {
                    reader.lines()
                        .filter(line -> line != null && !line.isBlank())
                        .forEach(entries::add);

                } catch (FileNotFoundException e) {
                    log.warn("Failed to read resource '{}': {}", resource, e.getMessage(), e);
                }
            }

            return entries;

        } catch (IOException e) {
            throw new IllegalStateException("ClassIndex: Cannot read class index", e);
        }
    }

}
