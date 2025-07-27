package org.framefork.typedIds.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;

final class ReflectionHacksTest
{

    @Nested
    final class ClassForName
    {

        @Test
        public void shouldReturnClassWhenExists()
        {
            Class<String> result = ReflectionHacks.classForName("java.lang.String");

            Assertions.assertNotNull(result);
            Assertions.assertEquals(String.class, result);
        }

        @Test
        public void shouldReturnNullWhenClassNotFound()
        {
            Class<Object> result = ReflectionHacks.classForName("com.nonexistent.Class");

            Assertions.assertNull(result);
        }

    }

    @Nested
    final class ClassExists
    {

        @Test
        public void shouldReturnTrueWhenClassExists()
        {
            boolean result = ReflectionHacks.classExists("java.lang.String");

            Assertions.assertTrue(result);
        }

        @Test
        public void shouldReturnFalseWhenClassDoesNotExist()
        {
            boolean result = ReflectionHacks.classExists("com.nonexistent.Class");

            Assertions.assertFalse(result);
        }

    }

    @Nested
    final class GetFieldType
    {

        @Test
        public void shouldReturnFieldTypeWhenFieldExists()
        {
            Class<?> result = ReflectionHacks.getFieldType(
                "org.framefork.typedIds.common.ReflectionHacksTest$TestClass",
                "stringField"
            );

            Assertions.assertEquals(String.class, result);
        }

        @Test
        public void shouldThrowWhenClassNotFound()
        {
            IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionHacks.getFieldType("com.nonexistent.Class", "field")
            );

            Assertions.assertInstanceOf(ClassNotFoundException.class, exception.getCause());
        }

        @Test
        public void shouldThrowWhenFieldNotFound()
        {
            IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionHacks.getFieldType(
                    "org.framefork.typedIds.common.ReflectionHacksTest$TestClass",
                    "nonexistentField"
                )
            );

            Assertions.assertInstanceOf(NoSuchFieldException.class, exception.getCause());
        }

    }

    @Nested
    final class GetFieldTypeChecked
    {

        @Test
        public void shouldReturnFieldTypeWhenAssignable()
        {
            Class<String> result = ReflectionHacks.getFieldTypeChecked(
                "org.framefork.typedIds.common.ReflectionHacksTest$TestClass",
                "stringField",
                String.class
            );

            Assertions.assertEquals(String.class, result);
        }

        @Test
        public void shouldReturnFieldTypeWhenSubtype()
        {
            Class<Serializable> result = ReflectionHacks.getFieldTypeChecked(
                "org.framefork.typedIds.common.ReflectionHacksTest$TestClass",
                "stringField",
                Serializable.class
            );

            Assertions.assertEquals(String.class, result);
        }

        @Test
        public void shouldThrowWhenNotAssignable()
        {
            IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionHacks.getFieldTypeChecked(
                    "org.framefork.typedIds.common.ReflectionHacksTest$TestClass",
                    "stringField",
                    Integer.class
                )
            );

            assertThat(exception).hasMessageContaining("is expected to be an instance of");
        }

    }

    @Nested
    final class GetConstructor
    {

        @Test
        public void shouldReturnConstructorHandle()
        {
            MethodHandle constructor = ReflectionHacks.getConstructor(TestClass.class, String.class);

            Assertions.assertNotNull(constructor);
            Assertions.assertEquals("MethodHandle(String)TestClass", constructor.toString());
        }

        @Test
        public void shouldReturnNoArgsConstructorHandle()
        {
            MethodHandle constructor = ReflectionHacks.getConstructor(TestClass.class);

            Assertions.assertNotNull(constructor);
            Assertions.assertEquals("MethodHandle()TestClass", constructor.toString());
        }

        @Test
        public void shouldThrowWhenConstructorNotFound()
        {
            IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> ReflectionHacks.getConstructor(TestClass.class, Integer.class, Long.class)
            );

            assertThat(exception)
                .hasMessageContaining("Cannot resolve constructor")
                .hasCauseInstanceOf(NoSuchMethodException.class);
        }

        @Test
        public void shouldInvokeConstructorSuccessfully() throws Throwable
        {
            MethodHandle constructor = ReflectionHacks.getConstructor(TestClass.class, String.class);

            var instance = (TestClass) constructor.invoke("test-value");

            Assertions.assertNotNull(instance);
            Assertions.assertEquals("test-value", instance.stringField);
        }

    }

    @Nested
    final class GetAllInterfaces
    {

        @Test
        public void shouldReturnAllInterfaces()
        {
            var instance = new TestClass();

            Class<?>[] interfaces = ReflectionHacks.getAllInterfaces(instance);

            assertThat(interfaces)
                .isNotEmpty()
                .contains(Serializable.class);
        }

        @Test
        public void shouldReturnEmptyForObject()
        {
            var instance = new Object();

            Class<?>[] interfaces = ReflectionHacks.getAllInterfaces(instance);

            Assertions.assertEquals(0, interfaces.length);
        }

        @Test
        public void shouldReturnInterfacesFromSuperclass()
        {
            var instance = new TestSubClass();

            Class<?>[] interfaces = ReflectionHacks.getAllInterfaces(instance);

            assertThat(interfaces)
                .isNotEmpty()
                .contains(Serializable.class, Comparable.class);
        }

    }

    public static class TestClass implements Serializable
    {

        public String stringField;

        public TestClass()
        {
            this.stringField = "default";
        }

        public TestClass(String stringField)
        {
            this.stringField = stringField;
        }

    }

    public static class TestSubClass extends TestClass implements Comparable<TestSubClass>
    {

        public TestSubClass()
        {
            super();
        }

        @Override
        public int compareTo(TestSubClass o)
        {
            return 0;
        }

    }

}
