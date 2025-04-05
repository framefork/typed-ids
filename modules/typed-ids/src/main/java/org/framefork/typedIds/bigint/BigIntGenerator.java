package org.framefork.typedIds.bigint;

import org.framefork.typedIds.bigint.random.HypersistenceTsidGeneratorFactory;
import org.framefork.typedIds.common.ServiceLoaderUtils;

public interface BigIntGenerator
{

    long generateRandom();

    /**
     * This could be useful, if you want e.g. deterministic IDs in your tests. See {@link ObjectBigIntId.Generators}
     */
    interface Factory
    {

        BigIntGenerator getGenerator(final ObjectBigIntId.Constructor<?> constructor);

        static Factory getDefault()
        {
            Factory globalFactory = ServiceLoaderUtils.getSingleOrNull(Factory.class);
            if (globalFactory != null) {
                return globalFactory;
            }

            if (HypersistenceTsidGeneratorFactory.AVAILABLE) {
                return new HypersistenceTsidGeneratorFactory();
            }

            throw new IllegalStateException("Please provide your own implementation of %s, or install one of the supported libraries".formatted(Factory.class.getName()));
        }

    }

}
