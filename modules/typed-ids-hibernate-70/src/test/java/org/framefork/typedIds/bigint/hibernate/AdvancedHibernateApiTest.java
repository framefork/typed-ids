package org.framefork.typedIds.bigint.hibernate;

import org.framefork.typedIds.bigint.hibernate.basic.BigIntDbIdentityGeneratedExplicitMappingEntity;
import org.framefork.typedIds.hibernate.tests.AbstractMySQLIntegrationTest;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class AdvancedHibernateApiTest extends AbstractMySQLIntegrationTest
{

    @Override
    protected Class<?>[] entities()
    {
        return new Class<?>[]{
            BigIntDbIdentityGeneratedExplicitMappingEntity.class,
        };
    }

    @Test
    public void testSessionGetReferenceProxyCreation()
    {
        // First, persist an entity and get its ID
        var savedId = doInJPA(em -> {
            var entity = new BigIntDbIdentityGeneratedExplicitMappingEntity("proxy-test-data");
            em.persist(entity);
            em.flush();
            return entity.getId();
        });

        // Test getReference proxy creation
        doInJPA(em -> {
            Session session = em.unwrap(Session.class);

            // Get reference should return a proxy without hitting the database
            BigIntDbIdentityGeneratedExplicitMappingEntity proxy =
                session.getReference(BigIntDbIdentityGeneratedExplicitMappingEntity.class, savedId);

            // Proxy should not be null
            Assertions.assertNotNull(proxy);

            // Proxy should not be initialized yet
            assertThat(Hibernate.isInitialized(proxy))
                .as("Proxy should not be initialized immediately after getReference()")
                .isFalse();

            // Accessing ID should not trigger initialization (ID is always available)
            BigIntDbIdentityGeneratedExplicitMappingEntity.Id proxyId = proxy.getId();
            assertThat(proxyId).isEqualTo(savedId);
            assertThat(Hibernate.isInitialized(proxy))
                .as("Accessing ID should not initialize proxy")
                .isFalse();

            // Accessing non-ID property should trigger lazy loading
            String data = proxy.getTitle();
            assertThat(data).isEqualTo("proxy-test-data");
            assertThat(Hibernate.isInitialized(proxy))
                .as("Accessing non-ID property should initialize proxy")
                .isTrue();
        });
    }

    @Test
    public void testSessionByMultipleIdsBatchLoading()
    {
        // Persist multiple entities
        var savedIds = doInJPA(em -> {
            List<BigIntDbIdentityGeneratedExplicitMappingEntity.Id> ids = new ArrayList<>();

            for (int i = 1; i <= 5; i++) {
                var entity = new BigIntDbIdentityGeneratedExplicitMappingEntity("batch-data-" + i);
                em.persist(entity);
                em.flush();
                ids.add(entity.getId());
            }

            return ids;
        });

        // Test batch loading with byMultipleIds
        doInJPA(em -> {
            Session session = em.unwrap(Session.class);

            // Load multiple entities by their IDs in a single operation
            List<BigIntDbIdentityGeneratedExplicitMappingEntity> entities =
                session.byMultipleIds(BigIntDbIdentityGeneratedExplicitMappingEntity.class)
                       .multiLoad(savedIds);

            // Should return all 5 entities
            assertThat(entities).hasSize(5);

            // All entities should be non-null and have correct IDs
            for (int i = 0; i < entities.size(); i++) {
                var entity = entities.get(i);
                Assertions.assertNotNull(entity);
                assertThat(savedIds).contains(entity.getId());
                assertThat(entity.getTitle()).startsWith("batch-data-");
            }

            // Verify that all saved IDs are represented
            List<BigIntDbIdentityGeneratedExplicitMappingEntity.Id> retrievedIds =
                entities.stream().map(BigIntDbIdentityGeneratedExplicitMappingEntity::getId).toList();

            assertThat(retrievedIds).containsExactlyInAnyOrderElementsOf(savedIds);
        });
    }

    @Test
    public void testSessionByMultipleIdsWithPartialResults()
    {
        // Persist one entity
        var existingId = doInJPA(em -> {
            var entity = new BigIntDbIdentityGeneratedExplicitMappingEntity("existing-entity");
            em.persist(entity);
            em.flush();
            return entity.getId();
        });

        // Create a non-existent ID (but valid type)
        var nonExistentId = BigIntDbIdentityGeneratedExplicitMappingEntity.Id.from(999999L);

        // Test batch loading with mix of existing and non-existing IDs
        doInJPA(em -> {
            Session session = em.unwrap(Session.class);

            var mixedIds = List.of(existingId, nonExistentId);

            var entities =
                session.byMultipleIds(BigIntDbIdentityGeneratedExplicitMappingEntity.class)
                       .multiLoad(mixedIds);

            // Should return only the existing entity, with null for non-existent
            assertThat(entities).hasSize(2);

            // One should be the actual entity, one should be null
            long nonNullCount = entities.stream().mapToLong(e -> e != null ? 1 : 0).sum();
            assertThat(nonNullCount).isEqualTo(1);

            // The non-null entity should have the existing ID
            var existingEntity = entities.stream().filter(e -> e != null).findFirst().orElse(null);
            Assertions.assertNotNull(existingEntity);
            assertThat(existingEntity.getId()).isEqualTo(existingId);
            assertThat(existingEntity.getTitle()).isEqualTo("existing-entity");
        });
    }

    @Test
    public void testNativeSqlParameterBinding()
    {
        // Persist an entity
        var savedId = doInJPA(em -> {
            var entity = new BigIntDbIdentityGeneratedExplicitMappingEntity("native-sql-test");
            em.persist(entity);
            em.flush();
            return entity.getId();
        });

        // Test native SQL query with custom ID parameter binding
        doInJPA(em -> {
            Session session = em.unwrap(Session.class);

            // Execute native SQL with custom ID as parameter
            String sql = "SELECT * FROM " + BigIntDbIdentityGeneratedExplicitMappingEntity.TABLE_NAME + " WHERE id = :id";

            BigIntDbIdentityGeneratedExplicitMappingEntity result =
                session.createNativeQuery(sql, BigIntDbIdentityGeneratedExplicitMappingEntity.class)
                       .setParameter("id", savedId.toLong())
                       .getSingleResult();

            // Verify the result
            Assertions.assertNotNull(result);
            assertThat(result.getId()).isEqualTo(savedId);
            assertThat(result.getTitle()).isEqualTo("native-sql-test");
        });
    }

    @Test
    public void testNativeSqlParameterBindingWithMultipleParams()
    {
        // Persist multiple entities
        var savedIds = doInJPA(em -> {
            List<BigIntDbIdentityGeneratedExplicitMappingEntity.Id> ids = new ArrayList<>();

            var entity1 = new BigIntDbIdentityGeneratedExplicitMappingEntity("sql-test-1");
            var entity2 = new BigIntDbIdentityGeneratedExplicitMappingEntity("sql-test-2");

            em.persist(entity1);
            em.persist(entity2);
            em.flush();

            ids.add(entity1.getId());
            ids.add(entity2.getId());

            return ids;
        });

        // Test native SQL with IN clause using custom IDs
        doInJPA(em -> {
            Session session = em.unwrap(Session.class);

            // Note: This tests the UserType's ability to handle collection parameter binding
            String sql = "SELECT * FROM " + BigIntDbIdentityGeneratedExplicitMappingEntity.TABLE_NAME +
                        " WHERE id IN (:ids) ORDER BY title";

            // Convert custom IDs to long values for native SQL
            List<Long> idValues = savedIds.stream().map(id -> id.toLong()).toList();

            @SuppressWarnings("unchecked")
            List<BigIntDbIdentityGeneratedExplicitMappingEntity> results =
                session.createNativeQuery(sql, BigIntDbIdentityGeneratedExplicitMappingEntity.class)
                       .setParameterList("ids", idValues)
                       .getResultList();

            // Verify results
            assertThat(results).hasSize(2);
            assertThat(results.get(0).getTitle()).isEqualTo("sql-test-1");
            assertThat(results.get(1).getTitle()).isEqualTo("sql-test-2");

            List<BigIntDbIdentityGeneratedExplicitMappingEntity.Id> resultIds =
                results.stream().map(BigIntDbIdentityGeneratedExplicitMappingEntity::getId).toList();

            assertThat(resultIds).containsExactlyInAnyOrderElementsOf(savedIds);
        });
    }

    @Test
    public void testNativeSqlWithCustomIdComparison()
    {
        // Persist an entity
        var savedId = doInJPA(em -> {
            var entity = new BigIntDbIdentityGeneratedExplicitMappingEntity("comparison-test");
            em.persist(entity);
            em.flush();
            return entity.getId();
        });

        // Test native SQL with comparison operators
        doInJPA(em -> {
            Session session = em.unwrap(Session.class);

            // Test greater than comparison
            String sql = "SELECT COUNT(*) FROM " + BigIntDbIdentityGeneratedExplicitMappingEntity.TABLE_NAME +
                        " WHERE id >= :minId";

            var count = session.createNativeQuery(sql, Long.class)
                                          .setParameter("minId", savedId.toLong())
                                          .getSingleResult();

            // Should find at least the entity we just created
            assertThat(count.longValue()).isGreaterThanOrEqualTo(1L);
        });
    }

}
