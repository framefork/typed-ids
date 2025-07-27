package org.framefork.typedIds;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("rawtypes")
final class TypedIdsRegistryTest
{

    @Nested
    final class GetObjectBigIntIdClasses
    {

        @Test
        public void shouldReturnListOfObjectBigIntIdClasses()
        {
            var result = TypedIdsRegistry.getObjectBigIntIdClasses();

            Assertions.assertNotNull(result);
        }

    }

    @Nested
    final class GetObjectUuidClasses
    {

        @Test
        public void shouldReturnListOfObjectUuidClasses()
        {
            var result = TypedIdsRegistry.getObjectUuidClasses();

            Assertions.assertNotNull(result);
        }

    }

}
