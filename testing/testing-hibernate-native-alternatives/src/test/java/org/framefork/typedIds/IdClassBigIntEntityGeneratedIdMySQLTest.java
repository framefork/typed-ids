package org.framefork.typedIds;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.framefork.typedIds.hibernate.tests.AbstractMySQLIntegrationTest;
import org.framefork.typedIds.idclass.IdClassBigInt;
import org.framefork.typedIds.idclass.IdClassBigIntEntityGeneratedId;
import org.hibernate.id.IdentifierGenerationException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@Disabled("This was passing with Hibernate 6.6, but with 7.0 it doesn't even boot due to stricter annotation validations on Hibernate startup")
class IdClassBigIntEntityGeneratedIdMySQLTest extends AbstractMySQLIntegrationTest
{

    @Override
    protected Class<?>[] entities()
    {
        return new Class<?>[]{
            IdClassBigIntEntityGeneratedId.class
        };
    }

    @Test
    void testSchemaGeneration()
    {
        doInJPA(em -> {
            var columnInfo = getIdColumnInfo(em, IdClassBigIntEntityGeneratedId.TABLE_NAME);
            assertThat(columnInfo.get("data_type", String.class)).describedAs("data_type").isEqualToIgnoringCase("bigint");
            assertThat(columnInfo.get("column_type", String.class)).describedAs("column_type").isEqualToIgnoringCase("bigint");
            assertThat(columnInfo.get("extra", String.class)).describedAs("extra").containsIgnoringCase("auto_increment");
        });
    }

    private static Tuple getIdColumnInfo(final EntityManager em, final String tableName)
    {
        return (Tuple) em.createNativeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE table_name = :table_name AND column_name = :column_name", Tuple.class)
            .setParameter("table_name", tableName)
            .setParameter("column_name", "value")
            .getSingleResult();
    }

    @Test
    void testIdClassWithGeneratedValueBehavior()
    {
        assertThatThrownBy(() -> doInJPA(em -> {
            var entity = new IdClassBigIntEntityGeneratedId("Test Product");

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
    void testIdClassQueryingSyntax()
    {
        doInJPA(em -> {
            // Insert test data via native SQL
            em.createNativeQuery("INSERT INTO " + IdClassBigIntEntityGeneratedId.TABLE_NAME + " (value, name) VALUES (42, 'Test Entity')")
                .executeUpdate();
            em.flush();
            em.clear();

            // Test different JPQL query syntaxes to see what works

            // 1. Try direct ID comparison with primitive value (this should work for @IdClass)
            var result = em.createQuery("SELECT e FROM IdClassBigIntEntityGeneratedId e WHERE e.value = :value", IdClassBigIntEntityGeneratedId.class)
                .setParameter("value", 42L)
                .getSingleResult();

            assertThat(result.getName()).isEqualTo("Test Entity");
            assertThat(result.getValue()).isEqualTo(42L);

            // 2. Try comparing with IdClass object (this fails - type mismatch)
            assertThatThrownBy(() -> {
                var searchId = new IdClassBigInt(42L);
                em.createQuery("SELECT e FROM IdClassBigIntEntityGeneratedId e WHERE e = :id", IdClassBigIntEntityGeneratedId.class)
                    .setParameter("id", searchId)
                    .getSingleResult();
            }).isInstanceOf(Exception.class)
                .hasMessageContaining("did not match parameter type");

            // 3. Alternative working syntax: Direct field access (same as #1)
            var result2 = em.createQuery("SELECT e FROM IdClassBigIntEntityGeneratedId e WHERE e.value = 42", IdClassBigIntEntityGeneratedId.class)
                .getSingleResult();

            assertThat(result2.getName()).isEqualTo("Test Entity");
        });
    }

    @Test
    void testSelectNewConstructorWithIdClass()
    {
        doInJPA(em -> {
            // Insert test data via native SQL
            em.createNativeQuery("INSERT INTO " + IdClassBigIntEntityGeneratedId.TABLE_NAME + " (value, name) VALUES (42, 'Test Entity')")
                .executeUpdate();
            em.flush();
            em.clear();

            // Test SELECT NEW constructor expression with IdClass

            // 1. Try selecting primitive value into DTO with primitive field (should work)
            var result1 = em.createQuery(
                    "SELECT NEW org.framefork.typedIds.IdClassBigIntEntityGeneratedIdMySQLTest$EntitySummaryWithPrimitiveDto(e.value, e.name) " +
                        "FROM IdClassBigIntEntityGeneratedId e WHERE e.value = 42",
                    EntitySummaryWithPrimitiveDto.class
                )
                .getSingleResult();

            assertThat(result1.idValue()).isEqualTo(42L);
            assertThat(result1.name()).isEqualTo("Test Entity");

            // 2. Try constructing IdClass object inline using the primitive value
            var result2 = em.createQuery(
                    "SELECT NEW org.framefork.typedIds.IdClassBigIntEntityGeneratedIdMySQLTest$EntitySummaryDto(NEW org.framefork.typedIds.idclass.IdClassBigInt(e.value), e.name) " +
                        "FROM IdClassBigIntEntityGeneratedId e WHERE e.value = 42",
                    EntitySummaryDto.class
                )
                .getSingleResult();

            assertThat(result2.id()).isNotNull();
            assertThat(result2.id().value()).isEqualTo(42L);
            assertThat(result2.name()).isEqualTo("Test Entity");

            // 3. Try mapping primitive field directly to IdClass VO (test automatic type conversion)
            assertThatThrownBy(() -> {
                em.createQuery(
                        "SELECT NEW org.framefork.typedIds.IdClassBigIntEntityGeneratedIdMySQLTest$EntitySummaryDto(e.value, e.name) " +
                            "FROM IdClassBigIntEntityGeneratedId e WHERE e.value = 42",
                        EntitySummaryDto.class
                    )
                    .getSingleResult();
            }).isInstanceOf(Exception.class)
                .hasMessageContaining("Missing constructor"); // Hibernate cannot auto-convert Long to IdClassBigInt
        });
    }

    /**
     * DTO record for testing SELECT NEW constructor expressions with IdClass types
     */
    public record EntitySummaryDto(
        IdClassBigInt id,
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
