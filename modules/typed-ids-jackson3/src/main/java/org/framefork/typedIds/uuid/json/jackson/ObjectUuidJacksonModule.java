package org.framefork.typedIds.uuid.json.jackson;

import com.fasterxml.jackson.annotation.JsonFormat;
import tools.jackson.core.Version;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.deser.Deserializers;
import tools.jackson.databind.ser.Serializers;
import com.google.auto.service.AutoService;
import org.framefork.typedIds.uuid.ObjectUuidTypeUtils;
import org.jspecify.annotations.Nullable;

@AutoService(JacksonModule.class)
public class ObjectUuidJacksonModule extends JacksonModule
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
        public ValueSerializer<?> findSerializer(
            final SerializationConfig config,
            final JavaType type,
            final BeanDescription.Supplier beanDescSupplier,
            final JsonFormat.Value formatOverrides
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

        @Override
        public boolean hasDeserializerFor(final DeserializationConfig config, final Class<?> valueType)
        {
            return ObjectUuidTypeUtils.getObjectUuidRawClass().isAssignableFrom(valueType);
        }

        @Nullable
        @Override
        public ValueDeserializer<?> findBeanDeserializer(
            final JavaType type,
            final DeserializationConfig config,
            final BeanDescription.Supplier beanDescSupplier
        )
        {
            if (ObjectUuidTypeUtils.getObjectUuidRawClass().isAssignableFrom(type.getRawClass())) {
                return new ObjectUuidDeserializer(type.getRawClass());
            }

            return null;
        }

    }

}
