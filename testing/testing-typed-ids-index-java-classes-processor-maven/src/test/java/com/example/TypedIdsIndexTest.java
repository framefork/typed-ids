package com.example;

import org.framefork.typedIds.TypedIdsRegistry;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Checks the index files that the processor wrote during this Maven build, and that {@code TypedIdsRegistry} reads them.
 */
final class TypedIdsIndexTest
{

    private static final String UUID_INDEX = "META-INF/services/org.framefork.typedIds.uuid.ObjectUuid";
    private static final String BIGINT_INDEX = "META-INF/services/org.framefork.typedIds.bigint.ObjectBigIntId";

    private static final String EXPECTED_UUID_INDEX = """
        com.example.Catalog$Section$Item$Id
        com.example.Person$Id
        com.example.TenantUserId
        com.example.UserId
        com.example.Warehouse$Id
        """;

    private static final String EXPECTED_BIGINT_INDEX = """
        com.example.Catalog$Section$Item$Sku
        com.example.OrderId
        com.example.Person$LegacyId
        com.example.TenantOrderId
        com.example.Warehouse$ShelfId
        """;

    @Test
    void uuidIndexFile() throws IOException
    {
        assertEquals(EXPECTED_UUID_INDEX, readSingleResource(UUID_INDEX));
    }

    @Test
    void bigIntIndexFile() throws IOException
    {
        assertEquals(EXPECTED_BIGINT_INDEX, readSingleResource(BIGINT_INDEX));
    }

    @Test
    void registryReturnsTheUuidClasses()
    {
        assertEquals(
            List.of(Catalog.Section.Item.Id.class, Person.Id.class, TenantUserId.class, UserId.class, Warehouse.Id.class),
            TypedIdsRegistry.getObjectUuidClasses()
        );
    }

    @Test
    void registryReturnsTheBigIntClasses()
    {
        assertEquals(
            List.of(Catalog.Section.Item.Sku.class, OrderId.class, Person.LegacyId.class, TenantOrderId.class, Warehouse.ShelfId.class),
            TypedIdsRegistry.getObjectBigIntIdClasses()
        );
    }

    /**
     * The index must exist exactly once on the test classpath, in target/classes: the test sources declare no ids, and typed-ids ships no index.
     */
    private static String readSingleResource(final String name) throws IOException
    {
        List<URL> resources = Collections.list(TypedIdsIndexTest.class.getClassLoader().getResources(name));
        assertEquals(1, resources.size(), () -> "copies of " + name + " on the classpath: " + resources);

        try (InputStream input = resources.get(0).openStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}
