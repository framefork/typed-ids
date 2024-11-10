package org.framefork.typedIds.uuid;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

final class ObjectUuidGenerator
{

    @NotNull
    static UUID randomUUid(final Function<?, ?> constructor)
    {
        // TODO: service-loaded impls?
        return V7GeneratorHolder.randomUUid(constructor);
    }

    /**
     * The generators are synchronized, but they don't have to be synchronized across the entire application.
     * Ideally we would have per-thread instance, but this is better than nothing.
     */
    private static final class V7GeneratorHolder
    {

        private static final ConcurrentHashMap<Function<?, ?>, NoArgGenerator> GENERATORS = new ConcurrentHashMap<>();

        static UUID randomUUid(final Function<?, ?> constructor)
        {
            return getGenerator(constructor).generate();
        }

        static NoArgGenerator getGenerator(final Function<?, ?> constructor)
        {
            return GENERATORS.computeIfAbsent(
                constructor,
                c -> Generators.timeBasedEpochRandomGenerator()
            );
        }

    }

}
