package org.framefork.typedIds.uuid.hibernate;

import org.framefork.typedIds.hibernate.tests.AbstractPostgreSQLIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import jakarta.persistence.Tuple;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class ObjectUuidTypePostgreSQLTest extends AbstractPostgreSQLIntegrationTest
{

    @Override
    protected Class<?>[] entities()
    {
        return new Class<?>[]{
            ArticleDefaultExplicitMapping.class,
        };
    }

    @Test
    public void testSchema()
    {
        doInJPA(em -> {
            var result = (Tuple) em.createNativeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = :table_name AND column_name = :column_name", Tuple.class)
                .setParameter("table_name", ArticleDefaultExplicitMapping.TABLE_NAME)
                .setParameter("column_name", "id")
                .getSingleResult();

            Assertions.assertEquals("uuid", result.get("data_type", String.class).toLowerCase());
        });
    }

    @Test
    public void testUsage()
    {
        Map<String, ArticleDefaultExplicitMapping.Id> idsByTitle = new HashMap<>();

        doInJPA(em -> {
            List<ArticleDefaultExplicitMapping> articles = List.of(
                new ArticleDefaultExplicitMapping("one"),
                new ArticleDefaultExplicitMapping("two"),
                new ArticleDefaultExplicitMapping("three")
            );

            articles.forEach(em::persist);
            articles.forEach(article -> idsByTitle.put(article.getTitle(), article.getId()));
            em.flush();
        });

        var idOfTwo = Objects.requireNonNull(idsByTitle.get("two"), "id must not be null");

        doInJPA(em -> {
            var article = em.find(ArticleDefaultExplicitMapping.class, idOfTwo);
            Assertions.assertEquals("two", article.getTitle());
        });

        doInJPA(em -> {
            var article = em.createQuery("SELECT a FROM ArticleDefaultExplicitMapping a WHERE a.id = :id", ArticleDefaultExplicitMapping.class)
                .setParameter("id", idOfTwo)
                .getSingleResult();
            Assertions.assertEquals("two", article.getTitle());
        });

        doInJPA(em -> {
            var articles = em.createQuery("SELECT a FROM ArticleDefaultExplicitMapping a WHERE a.id IN (:ids)", ArticleDefaultExplicitMapping.class)
                .setParameter("ids", List.copyOf(idsByTitle.values()))
                .getResultList();
            Assertions.assertEquals(3, articles.size());
        });
    }

}
