package org.framefork.typedIds.uuid.json.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.google.auto.service.AutoService;
import org.framefork.typedIds.uuid.ObjectUuidTypeUtils;
import org.jspecify.annotations.Nullable;

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
        context.addSerializers(new ObjectUuidSerializers());
        context.addDeserializers(new ObjectUuidDeserializers());
    }

    private static final class ObjectUuidSerializers extends Serializers.Base
    {

        @Nullable
        @Override
        public JsonSerializer<?> findSerializer(
            final SerializationConfig config,
            final JavaType type,
            final BeanDescription beanDesc
        )
        {
            if (ObjectUuidTypeUtils.getObjectUuidRawClass().isAssignableFrom(type.getRawClass())) {
                return new ObjectUuidSerializer();
            }

            return null;
        }

    }

    private static final class ObjectUuidDeserializers extends Deserializers.Base
    {

        @Nullable
        @Override
        public JsonDeserializer<?> findBeanDeserializer(
            final JavaType type,
            final DeserializationConfig config,
            final BeanDescription beanDesc
        )
        {
            if (ObjectUuidTypeUtils.getObjectUuidRawClass().isAssignableFrom(type.getRawClass())) {
                return new ObjectUuidDeserializer(type.getRawClass());
            }

            return null;
        }

    }

}
