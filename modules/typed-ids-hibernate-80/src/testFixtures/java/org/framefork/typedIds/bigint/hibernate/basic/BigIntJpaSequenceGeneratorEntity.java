package org.framefork.typedIds.bigint.hibernate.basic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.hibernate.ObjectBigIntIdType;
import org.hibernate.annotations.Type;
import org.jspecify.annotations.Nullable;

/**
 * A sequence-generated id with a non-default sequence name, initial value and allocation size from JPA {@code @SequenceGenerator}.
 */
@Entity
@Table(name = BigIntJpaSequenceGeneratorEntity.TABLE_NAME)
public class BigIntJpaSequenceGeneratorEntity
{

    public static final String TABLE_NAME = "bigint_jpa_sequence_generator";
    public static final String SEQUENCE_NAME = "bigint_jpa_custom_seq";
    public static final long INITIAL_VALUE = 100;
    public static final int INCREMENT_SIZE = 7;

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bigint_jpa_sequence_gen")
    @SequenceGenerator(name = "bigint_jpa_sequence_gen", sequenceName = SEQUENCE_NAME, initialValue = (int) INITIAL_VALUE, allocationSize = INCREMENT_SIZE)
    @Column(nullable = false)
    @Type(ObjectBigIntIdType.class)
    @Nullable
    private Id id;

    @Column(nullable = false)
    private String title;

    public BigIntJpaSequenceGeneratorEntity(final String title)
    {
        this.title = title;
    }

    @SuppressWarnings("NullAway")
    protected BigIntJpaSequenceGeneratorEntity()
    {
    }

    @Nullable
    public Id getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    public static final class Id extends ObjectBigIntId<Id>
    {

        private Id(final long inner)
        {
            super(inner);
        }

        public static Id from(final long value)
        {
            return ObjectBigIntId.fromLong(Id::new, value);
        }

    }

}
