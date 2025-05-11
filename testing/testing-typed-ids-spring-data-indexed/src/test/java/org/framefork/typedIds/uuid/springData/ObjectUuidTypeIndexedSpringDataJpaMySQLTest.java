package org.framefork.typedIds.uuid.springData;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.framefork.typedIds.springData.AbstractSpringDataMySQLTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class ObjectUuidTypeIndexedSpringDataJpaMySQLTest extends AbstractSpringDataMySQLTest
{

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UuidAppGeneratedExplicitMappingEntityRepository uuidAppGeneratedRepository;

    @Test
    public void testSchema()
    {
        var result = (Tuple) entityManager.createNativeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = :table_name AND column_name = :column_name", Tuple.class)
            .setParameter("table_name", UuidAppGeneratedExplicitMappingEntity.TABLE_NAME)
            .setParameter("column_name", "id")
            .getSingleResult();

        assertThat(result.get("data_type", String.class)).isEqualToIgnoringCase("binary");
        assertThat(result.get("column_type", String.class)).isEqualToIgnoringCase("binary(16)");
    }

    @Test
    public void testUsage()
    {
        Map<String, UuidAppGeneratedExplicitMappingEntity.Id> idsByTitle = new HashMap<>();

        var articles = List.of(
            new UuidAppGeneratedExplicitMappingEntity("one"),
            new UuidAppGeneratedExplicitMappingEntity("two"),
            new UuidAppGeneratedExplicitMappingEntity("three")
        );

        uuidAppGeneratedRepository.saveAll(articles);
        flushAndClear();

        articles.forEach(article -> idsByTitle.put(article.getTitle(), article.getId()));

        var idOfTwo = Objects.requireNonNull(idsByTitle.get("two"), "id must not be null");

        var article = uuidAppGeneratedRepository.findById(idOfTwo).orElseThrow();
        Assertions.assertEquals("two", article.getTitle());

        var articleByTitle = uuidAppGeneratedRepository.findByTitle("two");
        Assertions.assertEquals(idOfTwo, articleByTitle.getId());

        var allArticles = uuidAppGeneratedRepository.findAllById(idsByTitle.values());
        Assertions.assertEquals(3, allArticles.size());
    }
}
