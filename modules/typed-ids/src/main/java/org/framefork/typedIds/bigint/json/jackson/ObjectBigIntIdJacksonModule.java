package org.framefork.typedIds.bigint.json.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.google.auto.service.AutoService;
import org.framefork.typedIds.bigint.ObjectBigIntIdTypeUtils;

@AutoService(Module.class)
public class ObjectBigIntIdJacksonModule extends Module
{

    @Override
    public String getModuleName()
    {
        return ObjectBigIntIdJacksonModule.class.getSimpleName();
    }

    @Override
    public Version version()
    {
        return Version.unknownVersion();
    }

    @Override
    public void setupModule(final SetupContext context)
    {
        context.addSerializers(getSerializers());
        context.addDeserializers(getDeserializers());
    }

    private static SimpleSerializers getSerializers()
    {
        var serializers = new SimpleSerializers();

        serializers.addSerializer(ObjectBigIntIdTypeUtils.getObjectBigIntIdRawClass(), new ObjectBigIntIdSerializer());

        return serializers;
    }

    private static SimpleDeserializers getDeserializers()
    {
        return new SimpleDeserializers();
    }

}
