package org.framefork.typedIds.swagger;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;

import static org.assertj.core.api.Assertions.assertThat;

final class SwaggerCoreLineTest
{

    @Test
    void recognizesSwaggerCore2()
    {
        assertThat(SwaggerCoreLine.jsonMapperTypeName(getClass().getClassLoader())).isEqualTo(SwaggerCoreLine.JACKSON2_OBJECT_MAPPER);
        assertThat(SwaggerCoreLine.isSwaggerCore2(getClass().getClassLoader())).isTrue();
    }

    @Test
    void isFalseWithoutSwaggerCore() throws Exception
    {
        try (var withoutSwagger = new URLClassLoader(new URL[0], ClassLoader.getPlatformClassLoader())) {
            assertThat(SwaggerCoreLine.jsonMapperTypeName(withoutSwagger)).isNull();
            assertThat(SwaggerCoreLine.isSwaggerCore2(withoutSwagger)).isFalse();
        }
    }

}
