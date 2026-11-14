package org.framefork.typedIds.swagger;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Tells which swagger-core major is on the classpath.
 * <p>
 * Both majors ship {@code io.swagger.v3.core.converter.ModelConverter} under the same name, so the presence of a class cannot tell them apart.
 * What differs is the Jackson major they are built on: {@code io.swagger.v3.core.util.Json.mapper()} returns a Jackson 2
 * {@code com.fasterxml.jackson.databind.ObjectMapper} in 2.x and a Jackson 3 {@code tools.jackson.databind.ObjectMapper} in 3.x.
 * The check reads the declared return type by reflection, so it neither invokes the method nor creates swagger's mapper.
 */
@ApiStatus.Internal
public final class SwaggerCoreLine
{

    static final String JSON_CLASS_NAME = "io.swagger.v3.core.util.Json";
    static final String JACKSON2_OBJECT_MAPPER = "com.fasterxml.jackson.databind.ObjectMapper";

    private SwaggerCoreLine()
    {
    }

    /**
     * Whether swagger-core 2.x is visible to the given class loader; {@code false} when no swagger-core is.
     */
    public static boolean isSwaggerCore2(@Nullable final ClassLoader classLoader)
    {
        return JACKSON2_OBJECT_MAPPER.equals(jsonMapperTypeName(classLoader));
    }

    @Nullable
    static String jsonMapperTypeName(@Nullable final ClassLoader classLoader)
    {
        var effectiveClassLoader = classLoader != null ? classLoader : SwaggerCoreLine.class.getClassLoader();
        try {
            var jsonClass = Class.forName(JSON_CLASS_NAME, false, effectiveClassLoader);
            return jsonClass.getMethod("mapper").getReturnType().getName();

        } catch (ClassNotFoundException | NoSuchMethodException | LinkageError e) {
            return null;
        }
    }

}
