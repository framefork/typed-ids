package org.framefork.typedIds;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.framefork.typedIds.embeddable.EmbeddableBigIntWithGenerated;
import org.framefork.typedIds.embeddable.EntityEmbeddableBigIntGeneratedId;
import org.framefork.typedIds.hibernate.tests.AbstractMySQLIntegrationTest;
import org.hibernate.id.IdentifierGenerationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class EntityEmbeddableBigIntGeneratedIdMySQLTest extends AbstractMySQLIntegrationTest
{

    @Override
    protected Class<?>[] entities()
    {
        return new Class<?>[]{
            EntityEmbeddableBigIntGeneratedId.class
        };
    }

    @Test
    void testSchemaGeneration()
    {
        doInJPA(em -> {
            var columnInfo = getIdColumnInfo(em, EntityEmbeddableBigIntGeneratedId.TABLE_NAME);
            assertThat(columnInfo.get("data_type", String.class)).describedAs("data_type").isEqualToIgnoringCase("bigint");
            assertThat(columnInfo.get("column_type", String.class)).describedAs("column_type").isEqualToIgnoringCase("bigint");
            assertThat(columnInfo.get("extra", String.class)).describedAs("extra").containsIgnoringCase("auto_increment");
        });
    }

    private static Tuple getIdColumnInfo(final EntityManager em, final String tableName)
    {
        return (Tuple) em.createNativeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = :table_name AND column_name = :column_name", Tuple.class)
            .setParameter("table_name", tableName)
            .setParameter("column_name", "value")  // The embeddable field name
            .getSingleResult();
    }

    @Test
    void testEmbeddedIdWithGeneratedValueBehavior()
    {
        assertThatThrownBy(() -> doInJPA(em -> {
            var entity = new EntityEmbeddableBigIntGeneratedId("Test Product");

            // ID should be null before persistence
            assertThat(entity.getId()).isNull();

            // Try to persist
            em.persist(entity);
            em.flush();
        }))
            .isInstanceOf(IdentifierGenerationException.class)
            .hasMessageContaining("Identity generation isn't supported for composite ids");
    }

    @Test
    void testEmbeddedIdQueryingSyntax()
    {
        doInJPA(em -> {
            // Insert test data via native SQL
            em.createNativeQuery("INSERT INTO " + EntityEmbeddableBigIntGeneratedId.TABLE_NAME + " (value, name) VALUES (42, 'Test Entity')")
                .executeUpdate();
            em.flush();
            em.clear();

            // Test different JPQL query syntaxes to see what works

            // 1. Try direct ID comparison (this should fail for @EmbeddedId)
            assertThatThrownBy(() -> {
                em.createQuery("SELECT e FROM EntityEmbeddableBigIntGeneratedId e WHERE e.id = :id", EntityEmbeddableBigIntGeneratedId.class)
                    .setParameter("id", 42L)
                    .getSingleResult();
            }).isInstanceOf(Exception.class); // Will fail - can't compare embedded object to primitive

            // 2. Try comparing with embeddable object (this actually works!)
            var searchId = new EmbeddableBigIntWithGenerated(42L);
            var result1 = em.createQuery("SELECT e FROM EntityEmbeddableBigIntGeneratedId e WHERE e.id = :id", EntityEmbeddableBigIntGeneratedId.class)
                .setParameter("id", searchId)
                .getSingleResult();

            assertThat(result1.getName()).isEqualTo("Test Entity");
            assertThat(result1.getId()).isNotNull();
            assertThat(result1.getId().value()).isEqualTo(42L);

            // 3. Working syntax: Access the embedded field directly
            var result2 = em.createQuery("SELECT e FROM EntityEmbeddableBigIntGeneratedId e WHERE e.id.value = :value", EntityEmbeddableBigIntGeneratedId.class)
                .setParameter("value", 42L)
                .getSingleResult();

            assertThat(result2.getName()).isEqualTo("Test Entity");
            assertThat(result2.getId()).isNotNull();
            assertThat(result2.getId().value()).isEqualTo(42L);

            // 4. Alternative working syntax: Use member access in WHERE clause
            var result3 = em.createQuery("SELECT e FROM EntityEmbeddableBigIntGeneratedId e WHERE e.id.value = 42", EntityEmbeddableBigIntGeneratedId.class)
                .getSingleResult();

            assertThat(result3.getName()).isEqualTo("Test Entity");
        });
    }

    @Test
    void testSelectNewConstructorWithEmbeddedId()
    {
        doInJPA(em -> {
            // Insert test data via native SQL
            em.createNativeQuery("INSERT INTO " + EntityEmbeddableBigIntGeneratedId.TABLE_NAME + " (value, name) VALUES (42, 'Test Entity')")
                .executeUpdate();
            em.flush();
            em.clear();

            // Test SELECT NEW constructor expression with embedded ID

            // 1. Try passing the embedded ID object directly to DTO constructor
            var result1 = em.createQuery(
                    "SELECT NEW org.framefork.typedIds.EntityEmbeddableBigIntGeneratedIdMySQLTest$EntitySummaryDto(e.id, e.name) " +
                        "FROM EntityEmbeddableBigIntGeneratedId e WHERE e.id.value = 42",
                    EntitySummaryDto.class
                )
                .getSingleResult();

            assertThat(result1.id()).isNotNull();
            assertThat(result1.id().value()).isEqualTo(42L);
            assertThat(result1.name()).isEqualTo("Test Entity");

            // 2. Try constructing the embedded object inline using the primitive value (this actually works!)
            var result2 = em.createQuery(
                    "SELECT NEW org.framefork.typedIds.EntityEmbeddableBigIntGeneratedIdMySQLTest$EntitySummaryDto(NEW org.framefork.typedIds.embeddable.EmbeddableBigIntWithGenerated(e.id.value), e.name) " +
                        "FROM EntityEmbeddableBigIntGeneratedId e WHERE e.id.value = 42",
                    EntitySummaryDto.class
                )
                .getSingleResult();

            assertThat(result2.id()).isNotNull();
            assertThat(result2.id().value()).isEqualTo(42L);
            assertThat(result2.name()).isEqualTo("Test Entity");

            // 3. Alternative: Select into DTO with primitive value for comparison
            var result3 = em.createQuery(
                    "SELECT NEW org.framefork.typedIds.EntityEmbeddableBigIntGeneratedIdMySQLTest$EntitySummaryWithPrimitiveDto(e.id.value, e.name) " +
                        "FROM EntityEmbeddableBigIntGeneratedId e WHERE e.id.value = 42",
                    EntitySummaryWithPrimitiveDto.class
                )
                .getSingleResult();

            assertThat(result3.idValue()).isEqualTo(42L);
            assertThat(result3.name()).isEqualTo("Test Entity");
        });
    }

    /**
     * DTO record for testing SELECT NEW constructor expressions with embedded ID types
     */
    public record EntitySummaryDto(
        EmbeddableBigIntWithGenerated id,
        String name
    )
    {

    }

    /**
     * DTO record for testing SELECT NEW with primitive ID value
     */
    public record EntitySummaryWithPrimitiveDto(
        Long idValue,
        String name
    )
    {

    }

}
