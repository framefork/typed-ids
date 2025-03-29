package org.framefork.typedIds.uuid.random;

import com.fasterxml.uuid.Generators;
import org.framefork.typedIds.common.ReflectionHacks;
import org.framefork.typedIds.uuid.ObjectUuid;
import org.framefork.typedIds.uuid.UuidGenerator;

public final class FasterXmlUuidV7GeneratorFactory implements UuidGenerator.Factory
{

    public static final boolean AVAILABLE = ReflectionHacks.classExists("com.fasterxml.uuid.impl.TimeBasedEpochRandomGenerator");

    @Override
    public UuidGenerator getGenerator(final ObjectUuid.Constructor<?> constructor)
    {
        return Generators.timeBasedEpochRandomGenerator()::generate;
    }

}
