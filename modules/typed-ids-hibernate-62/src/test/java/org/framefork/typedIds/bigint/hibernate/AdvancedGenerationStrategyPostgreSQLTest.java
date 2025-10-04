package org.framefork.typedIds.bigint.hibernate;

import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.hibernate.basic.BigIntDbSequenceGeneratedExplicitMappingEntity;
import org.framefork.typedIds.bigint.hibernate.basic.BigIntPooledLoOptimizerEntity;
import org.framefork.typedIds.hibernate.tests.AbstractPostgreSQLIntegrationTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

final class AdvancedGenerationStrategyPostgreSQLTest extends AbstractPostgreSQLIntegrationTest
{

    @Override
    protected Class<?>[] entities()
    {
        return new Class<?>[]{
            BigIntPooledLoOptimizerEntity.class,
            BigIntDbSequenceGeneratedExplicitMappingEntity.class,
        };
    }

    @Override
    protected void additionalProperties(Properties properties)
    {
        // Enable JDBC batching for testing batching behavior
        properties.setProperty("hibernate.jdbc.batch_size", "20");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
    }

    @Test
    public void testPooledLoOptimizerPerformance()
    {
        // This test validates that the pooled-lo optimizer reduces database calls
        doInJPA(em -> {
            // Create 21 entities (more than allocation size of 20)
            List<BigIntPooledLoOptimizerEntity> entities = new ArrayList<>();
            for (int i = 1; i <= 21; i++) {
                entities.add(new BigIntPooledLoOptimizerEntity("data-" + i));
            }

            // Persist all entities
            entities.forEach(em::persist);
            em.flush();

            // Verify all entities have IDs assigned
            entities.forEach(entity -> {
                Assertions.assertNotNull(entity.getId(), "ID must not be null after persist");
            });

            // With pooled-lo optimizer and allocationSize=20:
            // - First 20 entities should get IDs from first sequence call
            // - 21st entity should get ID from second sequence call
            List<Long> idValues = entities.stream()
                .map(BigIntPooledLoOptimizerEntity::getId)
                .filter(Objects::nonNull)
                .map(ObjectBigIntId::toLong)
                .sorted()
                .toList();

            assertThat(idValues.get(0)).as("First ID should be 1").isEqualTo(1L);
            assertThat(idValues.get(20)).as("21st ID should be 21").isEqualTo(21L);
        });
    }

    @Test
    public void testSequenceStrategyBasicFunctionality()
    {
        doInJPA(em -> {
            // Create multiple entities with SEQUENCE generation
            List<BigIntDbSequenceGeneratedExplicitMappingEntity> entities = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                entities.add(new BigIntDbSequenceGeneratedExplicitMappingEntity("sequence-" + i));
            }

            // Verify IDs are null before persist
            entities.forEach(entity -> {
                Assertions.assertNull(entity.getId(), "ID should be null before persist");
            });

            // Persist all entities
            entities.forEach(em::persist);
            em.flush();

            // Verify all entities have IDs assigned from sequence
            entities.forEach(entity -> {
                Assertions.assertNotNull(entity.getId(), "ID must not be null after persist");
            });

            // All IDs should be unique and positive
            List<Long> idValues = entities.stream()
                .map(BigIntDbSequenceGeneratedExplicitMappingEntity::getId)
                .filter(Objects::nonNull)
                .map(ObjectBigIntId::toLong)
                .toList();

            assertThat(idValues).as("All IDs should be unique")
                .doesNotHaveDuplicates();

            assertThat(idValues).as("All IDs should be positive")
                .allMatch(id -> id > 0);
        });
    }

}
