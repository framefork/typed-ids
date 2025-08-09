package org.framefork.typedIds.swagger;

import com.fasterxml.jackson.databind.type.SimpleType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.converter.ModelConverterContextImpl;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.framefork.typedIds.TypedId;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.uuid.ObjectUuid;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

@SuppressWarnings("rawtypes")
@ApiStatus.Internal
public final class TypedIdsSchemaUtils
{

    public static final int UUID_LENGTH = 36;

    private final static ConcurrentHashMap<Class<?>, Schema> DEFAULT_SCHEMAS = new ConcurrentHashMap<>();

    private TypedIdsSchemaUtils()
    {
    }

    public static Schema createSchema(final Class<?> rawClass)
    {
        if (ObjectUuid.class.isAssignableFrom(rawClass)) {
            return createUuidSchema(rawClass);

        } else if (ObjectBigIntId.class.isAssignableFrom(rawClass)) {
            return createBigIntSchema(rawClass);
        }

        throw new IllegalArgumentException("Given class is not a subtype of %s, %s was given".formatted(TypedId.class, rawClass));
    }

    public static Schema createUuidSchema(final Class<?> rawClass)
    {
        // Map UUID-based typed IDs to primitive string:uuid in OpenAPI
        var result = new StringSchema()
            .format("uuid")
            .minLength(UUID_LENGTH)
            .maxLength(UUID_LENGTH);

        return applyDefaultSchema(result, getDefaultSchema(rawClass));
    }

    public static Schema createBigIntSchema(final Class<?> rawClass)
    {
        // Map bigint-based typed IDs to primitive integer:int64 in OpenAPI
        var result = new IntegerSchema()
            .format("int64");

        return applyDefaultSchema(result, getDefaultSchema(rawClass));
    }

    private static Schema applyDefaultSchema(final Schema result, final Schema defaultSchema)
    {
        Optional.ofNullable(defaultSchema.getName())
            .filter(Predicate.not(String::isBlank))
            .ifPresent(result::setName);
        Optional.ofNullable(defaultSchema.getDescription())
            .filter(Predicate.not(String::isBlank))
            .ifPresent(result::setDescription);
        return result;
    }

    public static Schema getDefaultSchema(final Class<?> rawClass)
    {
        return DEFAULT_SCHEMAS.computeIfAbsent(rawClass, TypedIdsSchemaUtils::computeDefaultSchema);
    }

    private static Schema computeDefaultSchema(final Class<?> rawClass)
    {
        var converterContext = getModelConverterContext();
        var schema = converterContext.resolve(
            new AnnotatedType()
                .type(rawClass)
                .resolveAsRef(false)
        );

        var schemaAnnotation = rawClass.getDeclaredAnnotation(io.swagger.v3.oas.annotations.media.Schema.class);
        var declaredName = Optional.ofNullable(schemaAnnotation).map(io.swagger.v3.oas.annotations.media.Schema::name).filter(Predicate.not(String::isBlank)).isPresent();
        if (!declaredName) {
            schema.setName(getAutomaticSchemaNameForClass(rawClass));

        } else {
            if (schema.getName() == null || schema.getName().isBlank()) {
                schema.setName(getAutomaticSchemaNameForClass(rawClass));
            }
        }

        if (schema.getDescription() == null || schema.getDescription().isBlank()) {
            schema.setDescription("ID of type " + schema.getName());
        }

        return schema;
    }

    @Nullable
    public static Class<?> rawClassOf(final Type type)
    {
        if (type instanceof SimpleType simpleType) {
            return simpleType.getRawClass();
        }
        if (type instanceof Class<?> c) {
            return c;
        }
        if (type instanceof ParameterizedType p) {
            final Type raw = p.getRawType();
            if (raw instanceof Class<?> c) {
                return c;
            }
        }
        return null;
    }

    private static ModelConverterContext getModelConverterContext()
    {
        var converters = ModelConverters.getInstance().getConverters().stream()
            .filter(converter -> !(converter instanceof TypedIdsModelConverter))
            .toList();
        return new ModelConverterContextImpl(converters);
    }

    /**
     * this automatically fixes names of `User.Id` which would otherwise become `Id` but we want it to be `UserId`
     */
    private static String getAutomaticSchemaNameForClass(final Class<?> clazz)
    {
        final Class<?> enclosing = clazz.getEnclosingClass();
        if (enclosing == null) {
            return clazz.getSimpleName();
        }
        final String outer = getAutomaticSchemaNameForClass(enclosing);
        final String inner = clazz.getSimpleName();
        return outer + "_" + inner;
    }

}
