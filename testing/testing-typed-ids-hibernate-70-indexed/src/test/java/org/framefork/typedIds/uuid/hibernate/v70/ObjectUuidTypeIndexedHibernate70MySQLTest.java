package org.framefork.typedIds.uuid.hibernate.v70;

import jakarta.persistence.Tuple;
import org.framefork.typedIds.hibernate.tests.AbstractMySQLIntegrationTest;
import org.framefork.typedIds.uuid.hibernate.UuidAppGeneratedExplicitMappingEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

final class ObjectUuidTypeIndexedHibernate70MySQLTest extends AbstractMySQLIntegrationTest
{

    @Override
    protected Class<?>[] entities()
    {
        return new Class<?>[]{
            UuidAppGeneratedExplicitMappingEntity.class,
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

            assertThat(result.get("data_type", String.class)).isEqualToIgnoringCase("binary");
            assertThat(result.get("column_type", String.class)).isEqualToIgnoringCase("binary(16)");
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

}
