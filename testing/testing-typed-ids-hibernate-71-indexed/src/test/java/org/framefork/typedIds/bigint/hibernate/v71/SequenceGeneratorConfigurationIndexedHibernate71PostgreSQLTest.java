package org.framefork.typedIds.bigint.hibernate.v71;

import org.framefork.testing.hibernate.AbstractPostgreSQLIntegrationTest;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.hibernate.basic.BigIntGenericSequenceGeneratorEntity;
import org.framefork.typedIds.bigint.hibernate.basic.BigIntJpaSequenceGeneratorEntity;
import org.framefork.typedIds.bigint.hibernate.basic.BigIntOwnSequenceGeneratorEntity;
import org.framefork.typedIds.bigint.hibernate.id.ObjectBigIntIdSequenceStyleGenerator;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.LongStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The sequence name, initial value and increment declared on the id's generator must be the ones that generate the ids.
 */
final class SequenceGeneratorConfigurationIndexedHibernate71PostgreSQLTest extends AbstractPostgreSQLIntegrationTest
{

    @Override
    protected Class<?>[] entities()
    {
        return new Class<?>[]{
            BigIntJpaSequenceGeneratorEntity.class,
            BigIntGenericSequenceGeneratorEntity.class,
            BigIntOwnSequenceGeneratorEntity.class,
        };
    }

    @Test
    public void jpaSequenceGeneratorConfigurationGeneratesTheIds()
    {
        var ids = persist(() -> new BigIntJpaSequenceGeneratorEntity("jpa"), BigIntJpaSequenceGeneratorEntity::getId);

        assertThat(ids).containsExactlyElementsOf(expectedIds(BigIntJpaSequenceGeneratorEntity.INITIAL_VALUE));
        assertThat(sequenceObjectNames())
            .contains(BigIntJpaSequenceGeneratorEntity.SEQUENCE_NAME)
            .doesNotContain(BigIntJpaSequenceGeneratorEntity.TABLE_NAME + "_seq");
        assertIncrementSize(BigIntJpaSequenceGeneratorEntity.class, BigIntJpaSequenceGeneratorEntity.SEQUENCE_NAME, BigIntJpaSequenceGeneratorEntity.INCREMENT_SIZE);
    }

    @Test
    public void genericGeneratorConfigurationGeneratesTheIds()
    {
        var ids = persist(() -> new BigIntGenericSequenceGeneratorEntity("generic"), BigIntGenericSequenceGeneratorEntity::getId);

        assertThat(ids).containsExactlyElementsOf(expectedIds(BigIntGenericSequenceGeneratorEntity.INITIAL_VALUE));
        assertThat(sequenceObjectNames())
            .contains(BigIntGenericSequenceGeneratorEntity.SEQUENCE_NAME)
            .doesNotContain(BigIntGenericSequenceGeneratorEntity.TABLE_NAME + "_seq");
        assertIncrementSize(BigIntGenericSequenceGeneratorEntity.class, BigIntGenericSequenceGeneratorEntity.SEQUENCE_NAME, BigIntGenericSequenceGeneratorEntity.INCREMENT_SIZE);
    }

    @Test
    public void generatorNamedByTheMappingItselfGeneratesTheIds()
    {
        var ids = persist(() -> new BigIntOwnSequenceGeneratorEntity("own"), BigIntOwnSequenceGeneratorEntity::getId);

        assertThat(ids).containsExactlyElementsOf(expectedIds(BigIntOwnSequenceGeneratorEntity.INITIAL_VALUE));
        assertThat(sequenceObjectNames())
            .contains(BigIntOwnSequenceGeneratorEntity.SEQUENCE_NAME)
            .doesNotContain(BigIntOwnSequenceGeneratorEntity.TABLE_NAME + "_seq");
        assertIncrementSize(BigIntOwnSequenceGeneratorEntity.class, BigIntOwnSequenceGeneratorEntity.SEQUENCE_NAME, BigIntOwnSequenceGeneratorEntity.INCREMENT_SIZE);
    }

    /**
     * Nine consecutive ids from the initial value; the increment is asserted separately, because the same ids also come out of a sequence with the default increment of 50.
     */
    private static List<Long> expectedIds(final long initialValue)
    {
        return LongStream.range(initialValue, initialValue + 9).boxed().toList();
    }

    /**
     * Asserts the increment of the database sequence and the generator.
     */
    private void assertIncrementSize(final Class<?> entityClass, final String sequenceName, final int incrementSize)
    {
        var generator = sessionFactory().unwrap(SessionFactoryImplementor.class)
            .getMappingMetamodel()
            .getEntityDescriptor(entityClass)
            .getGenerator();
        assertThat(generator).isInstanceOf(ObjectBigIntIdSequenceStyleGenerator.class);
        var sequenceGenerator = (SequenceStyleGenerator) generator;
        assertThat(sequenceGenerator.getDatabaseStructure().getIncrementSize()).as("database structure increment").isEqualTo(incrementSize);
        assertThat(sequenceGenerator.getOptimizer().getIncrementSize()).as("optimizer increment").isEqualTo(incrementSize);
        var databaseIncrement = doInJPA(em -> {
            return String.valueOf(em.createNativeQuery("select increment from information_schema.sequences where sequence_name = :name")
                .setParameter("name", sequenceName)
                .getSingleResult());
        });
        assertThat(databaseIncrement).as("increment of sequence %s", sequenceName).isEqualTo(String.valueOf(incrementSize));
    }

    private <E> List<Long> persist(final Supplier<E> factory, final Function<E, ? extends ObjectBigIntId<?>> idGetter)
    {
        return doInJPA(em -> {
            var ids = new ArrayList<Long>();
            for (int i = 0; i < 9; i++) {
                var entity = factory.get();
                em.persist(entity);
                ids.add(Objects.requireNonNull(idGetter.apply(entity), "id must not be null").toLong());
            }
            return ids;
        });
    }

    /**
     * The sequences.
     */
    private List<String> sequenceObjectNames()
    {
        return doInJPA(em -> {
            List<?> names = em.createNativeQuery("select sequence_name from information_schema.sequences").getResultList();
            return names.stream()
                .map(name -> String.valueOf(name).toLowerCase(Locale.ROOT))
                .toList();
        });
    }

}
