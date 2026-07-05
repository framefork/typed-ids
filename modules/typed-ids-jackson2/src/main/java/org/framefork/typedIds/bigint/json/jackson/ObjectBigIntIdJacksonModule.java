package org.framefork.typedIds.bigint.json.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.google.auto.service.AutoService;
import org.framefork.typedIds.bigint.ObjectBigIntIdTypeUtils;
import org.jspecify.annotations.Nullable;

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
        context.addSerializers(new ObjectBigIntIdSerializers());
        // Jackson is capable of deserializing the long, and then using the primary constructor without any additional help
    }

    private static final class ObjectBigIntIdSerializers extends Serializers.Base
    {

        @Nullable
        @Override
        public JsonSerializer<?> findSerializer(
            final SerializationConfig config,
            final JavaType type,
            final BeanDescription beanDesc
        )
        {
            if (ObjectBigIntIdTypeUtils.getObjectBigIntIdRawClass().isAssignableFrom(type.getRawClass())) {
                return new ObjectBigIntIdSerializer();
            }

            return null;
        }

    }

}
