package org.framefork.typedIds.bigint.hibernate;

import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.hibernate.basic.BigIntDbIdentityGeneratedExplicitMappingEntity;
import org.framefork.typedIds.bigint.hibernate.basic.BigIntNullIdEntity;
import org.framefork.typedIds.bigint.hibernate.basic.BigIntPooledLoOptimizerEntity;
import org.framefork.typedIds.hibernate.tests.AbstractMySQLIntegrationTest;
import org.hibernate.id.IdentifierGenerationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class AdvancedGenerationStrategyTest extends AbstractMySQLIntegrationTest
{

    @Override
    protected Class<?>[] entities()
    {
        return new Class<?>[]{
            BigIntPooledLoOptimizerEntity.class,
            BigIntDbIdentityGeneratedExplicitMappingEntity.class,
            BigIntNullIdEntity.class,
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
            // - First 20 entities should get IDs 1-20 from first sequence call
            // - 21st entity should get ID 21 from second sequence call
            // Validate that IDs are sequential and within expected ranges
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
    public void testIdentityStrategyBasicFunctionality()
    {
        doInJPA(em -> {
            // Create multiple entities with IDENTITY generation
            List<BigIntDbIdentityGeneratedExplicitMappingEntity> entities = new ArrayList<>();
            for (int i = 1; i <= 5; i++) {
                entities.add(new BigIntDbIdentityGeneratedExplicitMappingEntity("identity-" + i));
            }

            // Verify IDs are null before persist
            entities.forEach(entity -> {
                Assertions.assertNull(entity.getId(), "ID should be null before persist");
            });

            // Persist all entities
            entities.forEach(em::persist);
            em.flush();

            // Verify all entities have IDs assigned by database
            entities.forEach(entity -> {
                Assertions.assertNotNull(entity.getId(), "ID must not be null after persist");
            });

            // IDs should be sequential (MySQL AUTO_INCREMENT behavior)
            List<Long> idValues = entities.stream()
                .map(BigIntDbIdentityGeneratedExplicitMappingEntity::getId)
                .filter(Objects::nonNull)
                .map(ObjectBigIntId::toLong)
                .sorted()
                .toList();

            // Verify IDs are sequential starting from 1
            for (int i = 0; i < idValues.size(); i++) {
                assertThat(idValues.get(i)).as("ID should be sequential")
                    .isEqualTo((long) (i + 1));
            }
        });
    }


    @Test
    public void testNullIdRejectionForAssignedStrategy()
    {
        // Test that attempting to persist an entity with null ID throws appropriate exception
        var exception = assertThrows(
            IdentifierGenerationException.class,
            () -> doInJPA(em -> {
                var entity = new BigIntNullIdEntity("test-data");
                // ID is null by default
                Assertions.assertNull(entity.getId());

                em.persist(entity);
                em.flush(); // This should trigger the exception
            })
        );

        // Verify the exception message indicates the problem
        assertThat(exception.getMessage()).containsAnyOf("id", "identifier", "manually assigned");
    }

    @Test
    public void testAssignedStrategyWithValidId()
    {
        // Test successful persistence when ID is properly assigned
        BigIntNullIdEntity.Id assignedId = BigIntNullIdEntity.Id.random();

        doInJPA(em -> {
            var entity = new BigIntNullIdEntity("test-data");
            entity.setId(assignedId);

            em.persist(entity);
            em.flush();

            // Verify ID was preserved
            Assertions.assertEquals(assignedId, entity.getId());
        });

        // Verify entity can be retrieved by assigned ID
        doInJPA(em -> {
            var retrieved = em.find(BigIntNullIdEntity.class, assignedId);
            Assertions.assertNotNull(retrieved);
            Assertions.assertEquals("test-data", retrieved.getData());
            Assertions.assertEquals(assignedId, retrieved.getId());
        });
    }

}
