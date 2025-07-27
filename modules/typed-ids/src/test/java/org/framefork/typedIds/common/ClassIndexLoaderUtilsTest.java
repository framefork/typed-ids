package org.framefork.typedIds.common;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class ClassIndexLoaderUtilsTest
{

    @Nested
    final class GetIndexedSubclassesFor
    {

        @Test
        public void shouldReturnEmptyWhenNoIndexFound()
        {
            List<Class<? extends TestNotIndexedService>> result = ClassIndexLoaderUtils.getIndexedSubclassesFor(TestNotIndexedService.class);

            assertThat(result).hasSize(0);
        }

        @Test
        public void shouldReturnSingleWhenSingleServiceFound()
        {
            List<Class<? extends TestIndexedSingleImplClass>> result = ClassIndexLoaderUtils.getIndexedSubclassesFor(TestIndexedSingleImplClass.class);

            assertThat(result)
                .hasSize(1)
                .containsExactly(TestIndexedSingleImplSubclass1.class);
        }

        @Test
        public void shouldReturnSortedListByClassName()
        {
            List<Class<? extends TestIndexedMultipleImplsClass>> result = ClassIndexLoaderUtils.getIndexedSubclassesFor(TestIndexedMultipleImplsClass.class);

            assertThat(result)
                .hasSize(2)
                .containsExactly(
                    TestIndexedMultipleImplsSubclass1.class,
                    TestIndexedMultipleImplsSubclass2.class
                );
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
