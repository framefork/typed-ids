package org.framefork.typedIds.uuid;

import com.fasterxml.uuid.Generators;

import java.util.UUID;
import java.util.function.Function;

public interface UuidGenerator
{

    UUID generate();

    /**
     * This could be useful, if you want e.g. deterministic IDs in your tests. See {@link ObjectUuid.Generators}
     */
    interface Factory
    {

        UuidGenerator getGenerator(final Function<?, ?> constructor);

        final class DefaultUuidV7GeneratorFactory implements Factory
        {

            @Override
            public UuidGenerator getGenerator(final Function<?, ?> constructor)
            {
                return Generators.timeBasedEpochRandomGenerator()::generate;
            }

        }

    }

}
