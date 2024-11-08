package org.framefork.typedIds.uuid.json.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleDeserializers;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.google.auto.service.AutoService;
import org.framefork.typedIds.uuid.ObjectUuid;

@AutoService(Module.class)
public class ObjectUuidJacksonModule extends Module
{

    @Override
    public String getModuleName()
    {
        return ObjectUuidJacksonModule.class.getSimpleName();
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

        serializers.addSerializer(ObjectUuid.class, new ObjectUuidSerializer());

        return serializers;
    }

    private static SimpleDeserializers getDeserializers()
    {
        return new SimpleDeserializers();
    }

}
