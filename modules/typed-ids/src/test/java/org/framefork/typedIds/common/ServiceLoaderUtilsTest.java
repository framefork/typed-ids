package org.framefork.typedIds.common;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class ServiceLoaderUtilsTest
{

    @Nested
    final class GetSingleOrNull
    {

        @Test
        public void shouldReturnNullWhenNoServicesFound()
        {
            TestNotIndexedService result = ServiceLoaderUtils.getSingleOrNull(TestNotIndexedService.class);

            assertThat(result).isNull();
        }

        @Test
        public void shouldReturnNullWhenMultipleServicesFound()
        {
            var result = ServiceLoaderUtils.getSingleOrNull(TestIndexedMultipleImplsClass.class);

            assertThat(result).isNull();
        }

        @Test
        public void shouldReturnWhenSingleServiceFound()
        {
            var result = ServiceLoaderUtils.getSingleOrNull(TestIndexedSingleImplClass.class);

            assertThat(result).isInstanceOf(TestIndexedSingleImplSubclass1.class);
        }

    }

    public interface TestNotIndexedService
    {

    }

    public static class TestIndexedSingleImplClass
    {

    }

    public static class TestIndexedSingleImplSubclass1 extends TestIndexedSingleImplClass
    {

    }

    public static class TestIndexedMultipleImplsClass
    {

    }

    public static class TestIndexedMultipleImplsSubclass1 extends TestIndexedMultipleImplsClass
    {

    }

    public static class TestIndexedMultipleImplsSubclass2 extends TestIndexedMultipleImplsClass
    {

    }

}
