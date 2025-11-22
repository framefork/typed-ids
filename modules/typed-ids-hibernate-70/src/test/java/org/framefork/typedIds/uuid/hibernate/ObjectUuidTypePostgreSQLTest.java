package org.framefork.typedIds.uuid.hibernate;

import jakarta.persistence.Tuple;
import org.framefork.typedIds.hibernate.tests.AbstractPostgreSQLIntegrationTest;
import org.hibernate.id.IdentifierGenerationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class ObjectUuidTypePostgreSQLTest extends AbstractPostgreSQLIntegrationTest
{

    @Override
    protected Class<?>[] entities()
    {
        return new Class<?>[]{
            UuidAppGeneratedExplicitMappingEntity.class,
            UuidNullIdEntity.class,
        };
    }

    @Test
    public void testSchema()
    {
        doInJPA(em -> {
            var result = (Tuple) em.createNativeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = :table_name AND column_name = :column_name", Tuple.class)
                .setParameter("table_name", UuidAppGeneratedExplicitMappingEntity.TABLE_NAME)
                .setParameter("column_name", "id")
                .getSingleResult();

            assertThat(result.get("data_type", String.class)).isEqualToIgnoringCase("uuid");
        });
    }

    @Test
    public void testUsage()
    {
        Map<String, UuidAppGeneratedExplicitMappingEntity.Id> idsByTitle = new HashMap<>();

        doInJPA(em -> {
            var articles = List.of(
                new UuidAppGeneratedExplicitMappingEntity("one"),
                new UuidAppGeneratedExplicitMappingEntity("two"),
                new UuidAppGeneratedExplicitMappingEntity("three")
            );

            articles.forEach(em::persist);
            em.flush();

            articles.forEach(article -> idsByTitle.put(article.getTitle(), article.getId()));
        });

        var idOfTwo = Objects.requireNonNull(idsByTitle.get("two"), "id must not be null");

        doInJPA(em -> {
            var article = em.find(UuidAppGeneratedExplicitMappingEntity.class, idOfTwo);
            Assertions.assertEquals("two", article.getTitle());
        });

        doInJPA(em -> {
            var article = em.createQuery("SELECT a FROM UuidAppGeneratedExplicitMappingEntity a WHERE a.id = :id", UuidAppGeneratedExplicitMappingEntity.class)
                .setParameter("id", idOfTwo)
                .getSingleResult();
            Assertions.assertEquals("two", article.getTitle());
        });

        doInJPA(em -> {
            var articles = em.createQuery("SELECT a FROM UuidAppGeneratedExplicitMappingEntity a WHERE a.id IN (:ids)", UuidAppGeneratedExplicitMappingEntity.class)
                .setParameter("ids", List.copyOf(idsByTitle.values()))
                .getResultList();
            Assertions.assertEquals(3, articles.size());
        });
    }

    @Test
    public void testNullIdRejectionForAssignedStrategy()
    {
        // Test that attempting to persist an entity with null UUID ID throws appropriate exception
        var exception = assertThrows(
            IdentifierGenerationException.class,
            () -> doInJPA(em -> {
                var entity = new UuidNullIdEntity("test-data");
                // ID is null by default
                Assertions.assertNull(entity.getId());

                em.persist(entity);
                em.flush(); // This should trigger the exception
            })
        );

        // Verify the exception message indicates the problem
        assertThat(exception.getMessage()).contains("id");
    }

    @Test
    public void testAssignedStrategyWithValidUuidId()
    {
        // Test successful persistence when UUID ID is properly assigned
        var assignedId = UuidNullIdEntity.Id.random();

        doInJPA(em -> {
            var entity = new UuidNullIdEntity("test-data");
            entity.setId(assignedId);

            em.persist(entity);
            em.flush();

            // Verify ID was preserved
            Assertions.assertEquals(assignedId, entity.getId());
        });

        // Verify entity can be retrieved by assigned UUID ID
        doInJPA(em -> {
            var retrieved = em.find(UuidNullIdEntity.class, assignedId);
            Assertions.assertNotNull(retrieved);
            Assertions.assertEquals("test-data", retrieved.getData());
            Assertions.assertEquals(assignedId, retrieved.getId());
        });
    }

}
