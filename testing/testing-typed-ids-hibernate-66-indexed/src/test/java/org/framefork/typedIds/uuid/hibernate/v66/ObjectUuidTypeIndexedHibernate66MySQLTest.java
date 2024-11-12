package org.framefork.typedIds.uuid.hibernate.v66;

import org.framefork.typedIds.hibernate.tests.AbstractMySQLIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Tuple;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class ObjectUuidTypeIndexedHibernate66MySQLTest extends AbstractMySQLIntegrationTest
{

    @Override
    protected Class<?>[] entities()
    {
        return new Class<?>[]{
            ArticleTestingEntity.class,
        };
    }

    @Test
    public void testSchema()
    {
        doInJPA(em -> {
            var result = (Tuple) em.createNativeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = :table_name AND column_name = :column_name", Tuple.class)
                .setParameter("table_name", ArticleTestingEntity.TABLE_NAME)
                .setParameter("column_name", "id")
                .getSingleResult();

            Assertions.assertEquals("binaryyyy", result.get("data_type", String.class).toLowerCase());
            Assertions.assertEquals("binary(16)", result.get("column_type", String.class).toLowerCase());
        });
    }

    @Test
    public void testUsage()
    {
        Map<String, ArticleTestingEntity.Id> idsByTitle = new HashMap<>();

        doInJPA(em -> {
            List<ArticleTestingEntity> articles = List.of(
                new ArticleTestingEntity("one"),
                new ArticleTestingEntity("two"),
                new ArticleTestingEntity("three")
            );

            articles.forEach(em::persist);
            articles.forEach(article -> idsByTitle.put(article.getTitle(), article.getId()));
            em.flush();
        });

        var idOfTwo = Objects.requireNonNull(idsByTitle.get("two"), "id must not be null");

        doInJPA(em -> {
            var article = em.find(ArticleTestingEntity.class, idOfTwo);
            Assertions.assertEquals("two", article.getTitle());
        });

        doInJPA(em -> {
            var article = em.createQuery("SELECT a FROM ArticleTestingEntity a WHERE a.id = :id", ArticleTestingEntity.class)
                .setParameter("id", idOfTwo)
                .getSingleResult();
            Assertions.assertEquals("two", article.getTitle());
        });
    }

}
