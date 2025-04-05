package org.framefork.typedIds.bigint.hibernate.v62;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.framefork.typedIds.hibernate.tests.AbstractPostgreSQLIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

final class ObjectBigIntIdTypeIndexedHibernate62PostgreSQLTest extends AbstractPostgreSQLIntegrationTest
{

    @Override
    protected Class<?>[] entities()
    {
        return new Class<?>[]{
            BigIntAppGeneratedExplicitMappingEntity.class,
            BigIntDbAutoGeneratedExplicitMappingEntity.class,
            BigIntDbIdentityGeneratedExplicitMappingEntity.class,
            BigIntDbSequenceGeneratedExplicitMappingEntity.class,
        };
    }

    @Test
    public void testSchema()
    {
        doInJPA(em -> {
            var table1 = getIdColumnInfo(em, BigIntAppGeneratedExplicitMappingEntity.TABLE_NAME);
            assertThat(table1.get("data_type", String.class)).isEqualToIgnoringCase("bigint");

            var table2 = getIdColumnInfo(em, BigIntDbAutoGeneratedExplicitMappingEntity.TABLE_NAME);
            assertThat(table2.get("data_type", String.class)).isEqualToIgnoringCase("bigint");

            var table3 = getIdColumnInfo(em, BigIntDbIdentityGeneratedExplicitMappingEntity.TABLE_NAME);
            assertThat(table3.get("data_type", String.class)).isEqualToIgnoringCase("bigint");

            var table4 = getIdColumnInfo(em, BigIntDbSequenceGeneratedExplicitMappingEntity.TABLE_NAME);
            assertThat(table4.get("data_type", String.class)).isEqualToIgnoringCase("bigint");
        });
    }

    private static Tuple getIdColumnInfo(final EntityManager em, final String tableName)
    {
        return (Tuple) em.createNativeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = :table_name AND column_name = :column_name", Tuple.class)
            .setParameter("table_name", tableName)
            .setParameter("column_name", "id")
            .getSingleResult();
    }

    @Test
    public void testUsageAppGenerated()
    {
        Map<String, BigIntAppGeneratedExplicitMappingEntity.Id> idsByTitle = new HashMap<>();

        doInJPA(em -> {
            var articles = List.of(
                new BigIntAppGeneratedExplicitMappingEntity("one"),
                new BigIntAppGeneratedExplicitMappingEntity("two"),
                new BigIntAppGeneratedExplicitMappingEntity("three")
            );

            articles.forEach(em::persist);
            em.flush();

            articles.forEach(article -> idsByTitle.put(article.getTitle(), article.getId()));
        });

        var idOfTwo = Objects.requireNonNull(idsByTitle.get("two"), "id must not be null");

        doInJPA(em -> {
            var article = em.find(BigIntAppGeneratedExplicitMappingEntity.class, idOfTwo);
            Assertions.assertEquals("two", article.getTitle());
        });

        doInJPA(em -> {
            var article = em.createQuery("SELECT a FROM BigIntAppGeneratedExplicitMappingEntity a WHERE a.id = :id", BigIntAppGeneratedExplicitMappingEntity.class)
                .setParameter("id", idOfTwo)
                .getSingleResult();
            Assertions.assertEquals("two", article.getTitle());
        });

        doInJPA(em -> {
            var articles = em.createQuery("SELECT a FROM BigIntAppGeneratedExplicitMappingEntity a WHERE a.id IN (:ids)", BigIntAppGeneratedExplicitMappingEntity.class)
                .setParameter("ids", List.copyOf(idsByTitle.values()))
                .getResultList();
            Assertions.assertEquals(3, articles.size());
        });
    }

    @Test
    public void testUsageDbAutoGenerated()
    {
        Map<String, BigIntDbAutoGeneratedExplicitMappingEntity.Id> idsByTitle = new HashMap<>();

        doInJPA(em -> {
            var articles = List.of(
                new BigIntDbAutoGeneratedExplicitMappingEntity("one"),
                new BigIntDbAutoGeneratedExplicitMappingEntity("two"),
                new BigIntDbAutoGeneratedExplicitMappingEntity("three")
            );

            articles.forEach(article -> Assertions.assertNull(article.getId()));

            articles.forEach(em::persist);
            em.flush();

            articles.forEach(article -> Assertions.assertNotNull(article.getId()));

            articles.forEach(article -> idsByTitle.put(article.getTitle(), article.getId()));
        });

        var idOfTwo = Objects.requireNonNull(idsByTitle.get("two"), "id must not be null");

        doInJPA(em -> {
            var article = em.find(BigIntDbAutoGeneratedExplicitMappingEntity.class, idOfTwo);
            Assertions.assertEquals("two", article.getTitle());
        });

        doInJPA(em -> {
            var article = em.createQuery("SELECT a FROM BigIntDbAutoGeneratedExplicitMappingEntity a WHERE a.id = :id", BigIntDbAutoGeneratedExplicitMappingEntity.class)
                .setParameter("id", idOfTwo)
                .getSingleResult();
            Assertions.assertEquals("two", article.getTitle());
        });

        doInJPA(em -> {
            var articles = em.createQuery("SELECT a FROM BigIntDbAutoGeneratedExplicitMappingEntity a WHERE a.id IN (:ids)", BigIntDbAutoGeneratedExplicitMappingEntity.class)
                .setParameter("ids", List.copyOf(idsByTitle.values()))
                .getResultList();
            Assertions.assertEquals(3, articles.size());
        });
    }

    @Test
    public void testUsageDbIdentityGenerated()
    {
        Map<String, BigIntDbIdentityGeneratedExplicitMappingEntity.Id> idsByTitle = new HashMap<>();

        doInJPA(em -> {
            var articles = List.of(
                new BigIntDbIdentityGeneratedExplicitMappingEntity("one"),
                new BigIntDbIdentityGeneratedExplicitMappingEntity("two"),
                new BigIntDbIdentityGeneratedExplicitMappingEntity("three")
            );

            articles.forEach(article -> Assertions.assertNull(article.getId()));

            articles.forEach(em::persist);
            em.flush();

            articles.forEach(article -> Assertions.assertNotNull(article.getId()));

            articles.forEach(article -> idsByTitle.put(article.getTitle(), article.getId()));
        });

        var idOfTwo = Objects.requireNonNull(idsByTitle.get("two"), "id must not be null");

        doInJPA(em -> {
            var article = em.find(BigIntDbIdentityGeneratedExplicitMappingEntity.class, idOfTwo);
            Assertions.assertEquals("two", article.getTitle());
        });

        doInJPA(em -> {
            var article = em.createQuery("SELECT a FROM BigIntDbIdentityGeneratedExplicitMappingEntity a WHERE a.id = :id", BigIntDbIdentityGeneratedExplicitMappingEntity.class)
                .setParameter("id", idOfTwo)
                .getSingleResult();
            Assertions.assertEquals("two", article.getTitle());
        });

        doInJPA(em -> {
            var articles = em.createQuery("SELECT a FROM BigIntDbIdentityGeneratedExplicitMappingEntity a WHERE a.id IN (:ids)", BigIntDbIdentityGeneratedExplicitMappingEntity.class)
                .setParameter("ids", List.copyOf(idsByTitle.values()))
                .getResultList();
            Assertions.assertEquals(3, articles.size());
        });
    }

    @Test
    public void testUsageDbSequenceGenerated()
    {
        Map<String, BigIntDbSequenceGeneratedExplicitMappingEntity.Id> idsByTitle = new HashMap<>();

        doInJPA(em -> {
            var articles = List.of(
                new BigIntDbSequenceGeneratedExplicitMappingEntity("one"),
                new BigIntDbSequenceGeneratedExplicitMappingEntity("two"),
                new BigIntDbSequenceGeneratedExplicitMappingEntity("three")
            );

            articles.forEach(article -> Assertions.assertNull(article.getId()));

            articles.forEach(em::persist);
            em.flush();

            articles.forEach(article -> Assertions.assertNotNull(article.getId()));

            articles.forEach(article -> idsByTitle.put(article.getTitle(), article.getId()));
        });

        var idOfTwo = Objects.requireNonNull(idsByTitle.get("two"), "id must not be null");

        doInJPA(em -> {
            var article = em.find(BigIntDbSequenceGeneratedExplicitMappingEntity.class, idOfTwo);
            Assertions.assertEquals("two", article.getTitle());
        });

        doInJPA(em -> {
            var article = em.createQuery("SELECT a FROM BigIntDbSequenceGeneratedExplicitMappingEntity a WHERE a.id = :id", BigIntDbSequenceGeneratedExplicitMappingEntity.class)
                .setParameter("id", idOfTwo)
                .getSingleResult();
            Assertions.assertEquals("two", article.getTitle());
        });

        doInJPA(em -> {
            var articles = em.createQuery("SELECT a FROM BigIntDbSequenceGeneratedExplicitMappingEntity a WHERE a.id IN (:ids)", BigIntDbSequenceGeneratedExplicitMappingEntity.class)
                .setParameter("ids", List.copyOf(idsByTitle.values()))
                .getResultList();
            Assertions.assertEquals(3, articles.size());
        });
    }

}
