package org.framefork.typedIds.bigint.random;

import io.hypersistence.tsid.TSID;
import org.framefork.typedIds.bigint.BigIntGenerator;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.common.ReflectionHacks;

public final class HypersistenceTsidGeneratorFactory implements BigIntGenerator.Factory
{

    public static final boolean AVAILABLE = ReflectionHacks.classExists("io.hypersistence.tsid.TSID");

    @Override
    public BigIntGenerator getGenerator(final ObjectBigIntId.Constructor<?> constructor)
    {
        return () -> TSID.Factory.getTsid().toLong();
    }

}
