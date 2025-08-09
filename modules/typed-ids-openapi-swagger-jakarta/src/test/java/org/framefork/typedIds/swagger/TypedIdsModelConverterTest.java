package org.framefork.typedIds.swagger;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Schema;
import org.framefork.typedIds.swagger.bigint.BigIntAppGeneratedExplicitMappingEntity;
import org.framefork.typedIds.swagger.uuid.UuidAppGeneratedExplicitMappingEntity;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("rawtypes")
class TypedIdsModelConverterTest
{

    private static void registerConverter()
    {
        unregisterConverter();

        var converter = new TypedIdsModelConverter();
        ModelConverters.getInstance(true).addConverter(converter);
        ModelConverters.getInstance(false).addConverter(converter);
    }

    private static void unregisterConverter()
    {
        for (var convertersInstance : List.of(ModelConverters.getInstance(true), ModelConverters.getInstance(false))) {
            for (var converter : List.copyOf(convertersInstance.getConverters())) {
                if (converter instanceof TypedIdsModelConverter) {
                    convertersInstance.removeConverter(converter);
                }
            }
        }
    }

    @SuppressWarnings("UnnecessarilyFullyQualified")
    static Schema resolveSchema(final Class<?> impl, final Components components)
    {
        return AnnotationsUtils.resolveSchemaFromType(
            impl,
            components,
            null,
            false,
            impl.getDeclaredAnnotation(io.swagger.v3.oas.annotations.media.Schema.class),
            impl.getDeclaredAnnotation(io.swagger.v3.oas.annotations.media.ArraySchema.class),
            null
        );
    }

    @Nested
    class IdsInline
    {

        @Test
        void shouldResolveSchema()
        {
            try {
                // Given
                TypedIdsModelConverter.idsAsRef = false;
                registerConverter();

                // When
                var components = new Components();
                resolveSchema(UuidAppGeneratedExplicitMappingEntity.class, components);
                resolveSchema(BigIntAppGeneratedExplicitMappingEntity.class, components);

                // Then
                assertThat(components.getSchemas()).hasSize(2);

                var uuidWrapperSchema = Objects.requireNonNull(components.getSchemas().get(UuidAppGeneratedExplicitMappingEntity.class.getSimpleName()), "uuidWrapperSchema must not be null");
                var bigintWrapperSchema = Objects.requireNonNull(components.getSchemas().get(BigIntAppGeneratedExplicitMappingEntity.class.getSimpleName()), "bigintWrapperSchema must not be null");

                var uuidIdSchema = Objects.requireNonNull(uuidWrapperSchema.getProperties().get("id"), "uuidIdSchema must not be null");
                assertThat(uuidIdSchema)
                    .isNotNull()
                    .isInstanceOfAny(Schema.class)
                    .hasFieldOrPropertyWithValue("type", "string")
                    .hasFieldOrPropertyWithValue("format", "uuid");

                var bigintIdSchema = Objects.requireNonNull(bigintWrapperSchema.getProperties().get("id"), "bigintIdSchema must not be null");
                assertThat(bigintIdSchema)
                    .isNotNull()
                    .isInstanceOfAny(Schema.class)
                    .hasFieldOrPropertyWithValue("type", "integer")
                    .hasFieldOrPropertyWithValue("format", "int64");

            } finally {
                TypedIdsModelConverter.idsAsRef = true;
                unregisterConverter();
            }
        }

    }

    @Nested
    class IdsAsRefs
    {

        @Test
        void shouldResolveSchema()
        {
            try {
                // Given
                TypedIdsModelConverter.idsAsRef = true;
                registerConverter();

                // When
                var components = new Components();
                resolveSchema(UuidAppGeneratedExplicitMappingEntity.class, components);
                resolveSchema(BigIntAppGeneratedExplicitMappingEntity.class, components);

                // Then
                assertThat(components.getSchemas()).hasSize(4);

                var uuidWrapperSchema = Objects.requireNonNull(components.getSchemas().get(UuidAppGeneratedExplicitMappingEntity.class.getSimpleName()), "uuidWrapperSchema must not be null");
                var bigintWrapperSchema = Objects.requireNonNull(components.getSchemas().get(BigIntAppGeneratedExplicitMappingEntity.class.getSimpleName()), "bigintWrapperSchema must not be null");

                String uuidIdName = UuidAppGeneratedExplicitMappingEntity.class.getSimpleName() + "_Id";
                String bigintIdName = BigIntAppGeneratedExplicitMappingEntity.class.getSimpleName() + "_Id";

                assertThat(uuidWrapperSchema)
                    .extracting(s -> s.getProperties().get("id"))
                    .isInstanceOfAny(Schema.class)
                    .hasFieldOrPropertyWithValue("$ref", "#/components/schemas/" + uuidIdName);
                assertThat(bigintWrapperSchema)
                    .extracting(s -> s.getProperties().get("id"))
                    .isInstanceOfAny(Schema.class)
                    .hasFieldOrPropertyWithValue("$ref", "#/components/schemas/" + bigintIdName);

                var uuidIdSchema = Objects.requireNonNull(components.getSchemas().get(uuidIdName), "uuidWrapperSchema must not be null");
                var bigintIdSchema = Objects.requireNonNull(components.getSchemas().get(bigintIdName), "bigintWrapperSchema must not be null");

                assertThat(uuidIdSchema)
                    .isNotNull()
                    .isInstanceOfAny(Schema.class)
                    .hasFieldOrPropertyWithValue("type", "string")
                    .hasFieldOrPropertyWithValue("format", "uuid");
                assertThat(bigintIdSchema)
                    .isNotNull()
                    .isInstanceOfAny(Schema.class)
                    .hasFieldOrPropertyWithValue("type", "integer")
                    .hasFieldOrPropertyWithValue("format", "int64");

            } finally {
                TypedIdsModelConverter.idsAsRef = true;
                unregisterConverter();
            }
        }

    }

}
