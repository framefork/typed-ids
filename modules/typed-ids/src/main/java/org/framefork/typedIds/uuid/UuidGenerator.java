package org.framefork.typedIds.uuid;

import org.framefork.typedIds.common.ServiceLoaderUtils;
import org.framefork.typedIds.uuid.random.FasterXmlUuidV7GeneratorFactory;

import java.util.UUID;

public interface UuidGenerator
{

    UUID generateRandom();

    /**
     * This could be useful, if you want e.g. deterministic IDs in your tests. See {@link ObjectUuid.Generators}
     */
    interface Factory
    {

        UuidGenerator getGenerator(final ObjectUuid.Constructor<?> constructor);

        static Factory getDefault()
        {
            Factory globalFactory = ServiceLoaderUtils.getSingleOrNull(Factory.class);
            if (globalFactory != null) {
                return globalFactory;
            }

            if (FasterXmlUuidV7GeneratorFactory.AVAILABLE) {
                return new FasterXmlUuidV7GeneratorFactory();
            }

            throw new IllegalStateException("Please provide your own implementation of %s, or install one of the supported libraries".formatted(Factory.class.getName()));
        }

    }

}
