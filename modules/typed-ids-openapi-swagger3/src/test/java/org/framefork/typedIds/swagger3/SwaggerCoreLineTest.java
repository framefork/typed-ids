package org.framefork.typedIds.swagger3;

import org.junit.jupiter.api.Test;

import java.net.URL;
import java.net.URLClassLoader;

import static org.assertj.core.api.Assertions.assertThat;

final class SwaggerCoreLineTest
{

    @Test
    void recognizesSwaggerCore3()
    {
        assertThat(SwaggerCoreLine.jsonMapperTypeName(getClass().getClassLoader())).isEqualTo(SwaggerCoreLine.JACKSON3_OBJECT_MAPPER);
        assertThat(SwaggerCoreLine.isSwaggerCore3(getClass().getClassLoader())).isTrue();
    }

    @Test
    void isFalseWithoutSwaggerCore() throws Exception
    {
        try (var withoutSwagger = new URLClassLoader(new URL[0], ClassLoader.getPlatformClassLoader())) {
            assertThat(SwaggerCoreLine.jsonMapperTypeName(withoutSwagger)).isNull();
            assertThat(SwaggerCoreLine.isSwaggerCore3(withoutSwagger)).isFalse();
        }
    }

}
