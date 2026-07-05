package org.framefork.typedIds.bigint.json.jackson3;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.auto.service.AutoService;
import org.framefork.typedIds.bigint.ObjectBigIntIdTypeUtils;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.Version;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.deser.Deserializers;
import tools.jackson.databind.ser.Serializers;

@AutoService(JacksonModule.class)
public class ObjectBigIntIdJacksonModule extends JacksonModule
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
        // Unlike Jackson 2.x, Jackson 3.x no longer auto-detects the single-arg long constructor as a creator
        // (databind#5318), so an explicit deserializer is required here.
        context.addDeserializers(new ObjectBigIntIdDeserializers());
    }

    private static final class ObjectBigIntIdSerializers extends Serializers.Base
    {

        @Nullable
        @Override
        public ValueSerializer<?> findSerializer(
            final SerializationConfig config,
            final JavaType type,
            final BeanDescription.Supplier beanDescRef,
            final JsonFormat.Value formatOverrides
        )
        {
            if (ObjectBigIntIdTypeUtils.getObjectBigIntIdRawClass().isAssignableFrom(type.getRawClass())) {
                return new ObjectBigIntIdSerializer();
            }

            return null;
        }

    }

    private static final class ObjectBigIntIdDeserializers extends Deserializers.Base
    {

        @Nullable
        @Override
        public ValueDeserializer<?> findBeanDeserializer(
            final JavaType type,
            final DeserializationConfig config,
            final BeanDescription.Supplier beanDescRef
        )
        {
            if (ObjectBigIntIdTypeUtils.getObjectBigIntIdRawClass().isAssignableFrom(type.getRawClass())) {
                return new ObjectBigIntIdDeserializer(type.getRawClass());
            }

            return null;
        }

        @Override
        public boolean hasDeserializerFor(
            final DeserializationConfig config,
            final Class<?> valueType
        )
        {
            return ObjectBigIntIdTypeUtils.getObjectBigIntIdRawClass().isAssignableFrom(valueType);
        }

    }

}
