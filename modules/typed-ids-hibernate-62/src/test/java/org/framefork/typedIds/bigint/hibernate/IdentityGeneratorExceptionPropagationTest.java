package org.framefork.typedIds.bigint.hibernate;

import org.framefork.typedIds.bigint.hibernate.basic.BigIntDbIdentityGeneratedUniqueTitleEntity;
import org.framefork.typedIds.hibernate.tests.AbstractMySQLIntegrationTest;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.UndeclaredThrowableException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies that exceptions thrown during IDENTITY-based inserts
 * propagate without being wrapped in {@link UndeclaredThrowableException}.
 *
 * <p>The {@code ObjectBigIntIdIdentityGenerator} uses JDK Proxy to intercept
 * calls to Hibernate internals. Without proper unwrapping, {@link java.lang.reflect.Method#invoke}
 * wraps checked exceptions in {@link java.lang.reflect.InvocationTargetException},
 * which the JDK Proxy further wraps in {@link UndeclaredThrowableException},
 * masking the real cause (e.g. {@link org.hibernate.exception.ConstraintViolationException}).
 */
final class IdentityGeneratorExceptionPropagationTest extends AbstractMySQLIntegrationTest
{

    @Override
    protected Class<?>[] entities()
    {
        return new Class<?>[]{
            BigIntDbIdentityGeneratedUniqueTitleEntity.class,
        };
    }

    @Test
    public void constraintViolation_shouldNotBeWrappedInUndeclaredThrowableException()
    {
        // First, insert an entity with a unique title
        doInJPA(em -> {
            em.persist(new BigIntDbIdentityGeneratedUniqueTitleEntity("duplicate-title"));
            em.flush();
        });

        // Then, try to insert another entity with the same title to trigger a unique constraint violation
        var exception = assertThrows(
            Exception.class,
            () -> doInJPA(em -> {
                em.persist(new BigIntDbIdentityGeneratedUniqueTitleEntity("duplicate-title"));
                em.flush();
            })
        );

        // The exception must NOT be UndeclaredThrowableException - that would mean
        // InvocationTargetException was not properly unwrapped in the JDK Proxy handler
        assertThat(exception)
            .as("Exception should not be wrapped in UndeclaredThrowableException")
            .isNotInstanceOf(UndeclaredThrowableException.class);

        // Verify the real constraint violation exception is present in the chain
        assertThat(hasExceptionInChain(exception, ConstraintViolationException.class))
            .as("ConstraintViolationException should be in the exception chain, but got: %s", exception)
            .isTrue();
    }

    private static boolean hasExceptionInChain(final Throwable throwable, final Class<? extends Throwable> expectedType)
    {
        Throwable current = throwable;
        while (current != null) {
            if (expectedType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

}
