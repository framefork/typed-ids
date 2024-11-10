package org.framefork.typedIds.uuid.hibernate.v64;

import org.framefork.typedIds.hibernate.tests.AbstractPostgreSQLIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class ObjectUuidTypeWithHibernate64Test extends AbstractPostgreSQLIntegrationTest
{

    @Override
    protected Class<?>[] entities()
    {
        return new Class<?>[]{
            ArticleTestingEntity.class,
        };
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
