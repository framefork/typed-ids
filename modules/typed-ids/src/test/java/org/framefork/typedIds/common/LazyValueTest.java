package org.framefork.typedIds.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

final class LazyValueTest
{

    @Nested
    final class BasicFunctionality
    {

        @Test
        public void shouldInitializeValueOnFirstAccess()
        {
            var callCount = new AtomicInteger(0);
            LazyValue<String> lazyValue = new LazyValue<>(() -> {
                callCount.incrementAndGet();
                return "test-value";
            });

            Assertions.assertEquals(0, callCount.get());

            String result = lazyValue.get();

            Assertions.assertEquals("test-value", result);
            Assertions.assertEquals(1, callCount.get());
        }

        @Test
        public void shouldReturnSameInstanceOnMultipleCalls()
        {
            var callCount = new AtomicInteger(0);
            LazyValue<String> lazyValue = new LazyValue<>(() -> {
                callCount.incrementAndGet();
                return "test-value";
            });

            Assertions.assertEquals(0, callCount.get());

            String first = lazyValue.get();
            String second = lazyValue.get();
            String third = lazyValue.get();

            Assertions.assertSame(first, second);
            Assertions.assertSame(second, third);
            Assertions.assertEquals(1, callCount.get());
        }

    }

    @Nested
    final class SetMethod
    {

        @Test
        public void shouldOverrideValueWhenSet()
        {
            var callCount = new AtomicInteger(0);
            LazyValue<String> lazyValue = new LazyValue<>(() -> {
                callCount.incrementAndGet();
                return "initial-value";
            });

            lazyValue.set("override-value");
            String result = lazyValue.get();

            Assertions.assertEquals("override-value", result);
            Assertions.assertEquals(0, callCount.get());
        }

        @Test
        public void shouldOverrideInitializedValue()
        {
            LazyValue<String> lazyValue = new LazyValue<>(() -> "initial-value");

            String initialResult = lazyValue.get();
            lazyValue.set("override-value");
            String overriddenResult = lazyValue.get();

            Assertions.assertEquals("initial-value", initialResult);
            Assertions.assertEquals("override-value", overriddenResult);
        }

    }

    @Nested
    final class NullHandling
    {

        @Test
        @SuppressWarnings("NullAway")
        public void shouldThrowWhenSupplierIsNull()
        {
            Assertions.assertThrows(
                NullPointerException.class,
                () -> new LazyValue<>(null),
                "initialValueSupplier must not be null"
            );
        }

        @Test
        public void shouldThrowWhenSupplierReturnsNull()
        {
            LazyValue<String> lazyValue = new LazyValue<>(() -> null);

            NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                lazyValue::get
            );

            assertThat(exception).hasMessage("initialValueSupplier must not return null");
        }

        @Test
        @SuppressWarnings("NullAway")
        public void shouldThrowWhenSetWithNull()
        {
            LazyValue<String> lazyValue = new LazyValue<>(() -> "test");

            Assertions.assertThrows(
                NullPointerException.class,
                () -> lazyValue.set(null),
                "newValue must not be null"
            );
        }

    }

    @Nested
    final class ThreadSafety
    {

        @Test
        public void shouldHandleConcurrentSetAndGet() throws InterruptedException
        {
            LazyValue<String> lazyValue = new LazyValue<>(() -> "initial");
            var startLatch = new CountDownLatch(1);
            var finishLatch = new CountDownLatch(20);

            ExecutorService executor = Executors.newFixedThreadPool(20);

            for (int i = 0; i < 10; i++) {
                int index = i;
                @SuppressWarnings("unused")
                Future<?> future1 = executor.submit(() -> {
                    try {
                        startLatch.await();
                        lazyValue.set("set-" + index);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        finishLatch.countDown();
                    }
                });
            }

            for (int i = 0; i < 10; i++) {
                @SuppressWarnings("unused")
                Future<?> future2 = executor.submit(() -> {
                    try {
                        startLatch.await();
                        String result = lazyValue.get();
                        Assertions.assertNotNull(result);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        finishLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            Assertions.assertTrue(finishLatch.await(5, TimeUnit.SECONDS));

            executor.shutdown();
            Assertions.assertTrue(executor.awaitTermination(1, TimeUnit.SECONDS));

            String finalValue = lazyValue.get();
            Assertions.assertNotNull(finalValue);
        }

    }

    @Nested
    final class ExceptionHandling
    {

        @Test
        public void shouldPropagateSupplierException()
        {
            LazyValue<String> lazyValue = new LazyValue<>(() -> {
                throw new RuntimeException("Test exception");
            });

            RuntimeException exception = Assertions.assertThrows(
                RuntimeException.class,
                lazyValue::get
            );

            assertThat(exception).hasMessage("Test exception");
        }

        @Test
        public void shouldRethrowExceptionOnSubsequentCalls()
        {
            var callCount = new AtomicInteger(0);
            LazyValue<String> lazyValue = new LazyValue<>(() -> {
                callCount.incrementAndGet();
                throw new RuntimeException("Test exception");
            });

            Assertions.assertThrows(RuntimeException.class, lazyValue::get);
            Assertions.assertThrows(RuntimeException.class, lazyValue::get);

            Assertions.assertEquals(2, callCount.get());
        }

    }

}
