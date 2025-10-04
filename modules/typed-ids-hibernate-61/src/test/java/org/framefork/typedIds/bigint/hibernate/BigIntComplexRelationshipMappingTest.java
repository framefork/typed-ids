package org.framefork.typedIds.bigint.hibernate;

import org.framefork.typedIds.bigint.hibernate.composite.BigIntCompositeEmbeddedIdEntity;
import org.framefork.typedIds.bigint.hibernate.composite.BigIntCompositeIdClassEntity;
import org.framefork.typedIds.bigint.hibernate.composite.BigIntCompositePK;
import org.framefork.typedIds.bigint.hibernate.composite.BigIntIdClassPK;
import org.framefork.typedIds.bigint.hibernate.relationships.BigIntChildEntity;
import org.framefork.typedIds.bigint.hibernate.relationships.BigIntParentEntity;
import org.framefork.typedIds.bigint.hibernate.relationships.BigIntPersonDetailsEntity;
import org.framefork.typedIds.bigint.hibernate.relationships.BigIntPersonEntity;
import org.framefork.typedIds.hibernate.tests.AbstractMySQLIntegrationTest;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

final class BigIntComplexRelationshipMappingTest extends AbstractMySQLIntegrationTest
{

    @Override
    protected Class<?>[] entities()
    {
        return new Class<?>[]{
            BigIntParentEntity.class,
            BigIntChildEntity.class,
            BigIntPersonEntity.class,
            BigIntPersonDetailsEntity.class,
            BigIntCompositeEmbeddedIdEntity.class,
            BigIntCompositeIdClassEntity.class,
        };
    }

    @Test
    public void testManyToOneRelationshipWithCustomIdAsForeignKey()
    {
        // Create and persist parent entity
        doInJPA(em -> {
            var parent = new BigIntParentEntity("Test Parent");
            em.persist(parent);
            em.flush();

            Assertions.assertNotNull(parent.getId());
            return parent.getId();
        });

        // Store parent ID for later use
        var parentId = doInJPA(em -> {
            var parent = em.createQuery("SELECT p FROM BigIntParentEntity p WHERE p.name = :name", BigIntParentEntity.class)
                .setParameter("name", "Test Parent")
                .getSingleResult();
            return parent.getId();
        });

        // Create child entity with reference to parent
        doInJPA(em -> {
            var parent = em.find(BigIntParentEntity.class, parentId);
            Assertions.assertNotNull(parent);

            var child = new BigIntChildEntity("Child Data");
            child.setParent(parent);
            em.persist(child);
            em.flush();

            Assertions.assertNotNull(child.getId());
        });

        // Test lazy loading behavior
        doInJPA(em -> {
            var child = em.createQuery("SELECT c FROM BigIntChildEntity c WHERE c.data = :data", BigIntChildEntity.class)
                .setParameter("data", "Child Data")
                .getSingleResult();

            // Parent should be lazy-loaded proxy
            var parent = child.getParent();
            Assertions.assertNotNull(parent);
            assertThat(Hibernate.isInitialized(parent)).isFalse();

            // Accessing parent's name should trigger lazy initialization
            var retrievedParentName = parent.getName();
            assertThat(Hibernate.isInitialized(parent)).isTrue();
            assertThat(retrievedParentName).isEqualTo("Test Parent");
            assertThat(parent.getId()).isEqualTo(parentId);
        });
    }

    @Test
    public void testOneToOneWithMapsIdDerivedIdentity()
    {
        // Create and persist person entity
        doInJPA(em -> {
            var person = new BigIntPersonEntity("John", "Doe");
            em.persist(person);
            em.flush();

            Assertions.assertNotNull(person.getId());
            return person.getId();
        });

        // Get the persisted person ID
        var personId = doInJPA(em -> {
            var person = em.createQuery("SELECT p FROM BigIntPersonEntity p WHERE p.firstName = :firstName", BigIntPersonEntity.class)
                .setParameter("firstName", "John")
                .getSingleResult();
            return person.getId();
        });

        // Create PersonDetails with derived identity
        doInJPA(em -> {
            var person = em.find(BigIntPersonEntity.class, personId);
            Assertions.assertNotNull(person);

            var details = new BigIntPersonDetailsEntity("john.doe@example.com", "+1-555-0123");
            details.setPerson(person);

            // ID should be null before persist
            Assertions.assertNull(details.getId());

            em.persist(details);
            em.flush();

            // ID should now be derived from Person
            Assertions.assertNotNull(details.getId());
            assertThat(details.getId()).isEqualTo(personId);
        });

        // Test retrieval using derived ID
        doInJPA(em -> {
            var details = em.find(BigIntPersonDetailsEntity.class, personId);
            Assertions.assertNotNull(details);
            assertThat(details.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(details.getId()).isEqualTo(personId);
        });
    }

    @Test
    public void testCompositeKeyWithEmbeddedId()
    {
        var customId = BigIntCompositePK.Id.random();
        var compositePK = new BigIntCompositePK(customId, "part-A");

        // Create and persist entity with composite key
        doInJPA(em -> {
            var entity = new BigIntCompositeEmbeddedIdEntity(compositePK, "Test Data");
            em.persist(entity);
            em.flush();
        });

        // Test retrieval by composite key
        doInJPA(em -> {
            var retrieved = em.find(BigIntCompositeEmbeddedIdEntity.class, compositePK);
            Assertions.assertNotNull(retrieved);
            assertThat(retrieved.getData()).isEqualTo("Test Data");
            assertThat(retrieved.getId()).isEqualTo(compositePK);
        });

        // Test first-level cache integrity (critical test)
        doInJPA(em -> {
            var entity1 = em.find(BigIntCompositeEmbeddedIdEntity.class, compositePK);
            var entity2 = em.find(BigIntCompositeEmbeddedIdEntity.class, compositePK);

            // Should be the exact same instance due to persistence context caching
            Assertions.assertSame(entity1, entity2,
                "First-level cache should return same instance for same composite key");
        });
    }

    @Test
    public void testCompositeKeyWithIdClass()
    {
        var customId = BigIntIdClassPK.Id.random();
        var idClassPK = new BigIntIdClassPK(customId, "part-B");

        // Create and persist entity with @IdClass
        doInJPA(em -> {
            var entity = new BigIntCompositeIdClassEntity(customId, "part-B", "IdClass Test Data");
            em.persist(entity);
            em.flush();
        });

        // Test retrieval by composite key
        doInJPA(em -> {
            var retrieved = em.find(BigIntCompositeIdClassEntity.class, idClassPK);
            Assertions.assertNotNull(retrieved);
            assertThat(retrieved.getData()).isEqualTo("IdClass Test Data");
            assertThat(retrieved.getCustomIdPart()).isEqualTo(customId);
            assertThat(retrieved.getStringPart()).isEqualTo("part-B");
        });

        // Test first-level cache integrity (critical test)
        doInJPA(em -> {
            var entity1 = em.find(BigIntCompositeIdClassEntity.class, idClassPK);
            var entity2 = em.find(BigIntCompositeIdClassEntity.class, idClassPK);

            // Should be the exact same instance due to persistence context caching
            Assertions.assertSame(entity1, entity2,
                "First-level cache should return same instance for same @IdClass composite key");
        });
    }

    @Test
    public void testCompositeKeyEqualityAndHashCode()
    {
        var customId1 = BigIntCompositePK.Id.random();
        var customId2 = BigIntCompositePK.Id.random();

        var pk1a = new BigIntCompositePK(customId1, "test");
        var pk1b = new BigIntCompositePK(customId1, "test");
        var pk2 = new BigIntCompositePK(customId2, "test");
        var pk3 = new BigIntCompositePK(customId1, "different");

        // Test equals
        assertThat(pk1a).isEqualTo(pk1b);
        assertThat(pk1a).isNotEqualTo(pk2);
        assertThat(pk1a).isNotEqualTo(pk3);

        // Test hashCode consistency
        assertThat(pk1a.hashCode()).isEqualTo(pk1b.hashCode());

        var customId3 = BigIntIdClassPK.Id.random();
        var customId4 = BigIntIdClassPK.Id.random();

        // Similar tests for IdClass
        var idPk1a = new BigIntIdClassPK(customId3, "test");
        var idPk1b = new BigIntIdClassPK(customId3, "test");
        var idPk2 = new BigIntIdClassPK(customId4, "test");

        assertThat(idPk1a).isEqualTo(idPk1b);
        assertThat(idPk1a).isNotEqualTo(idPk2);
        assertThat(idPk1a.hashCode()).isEqualTo(idPk1b.hashCode());
    }

}
