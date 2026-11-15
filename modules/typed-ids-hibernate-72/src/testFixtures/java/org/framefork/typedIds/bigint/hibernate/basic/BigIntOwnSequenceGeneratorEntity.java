package org.framefork.typedIds.bigint.hibernate.basic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.hibernate.ObjectBigIntIdType;
import org.framefork.typedIds.bigint.hibernate.id.ObjectBigIntIdSequenceStyleGenerator;
import org.hibernate.annotations.Type;
import org.jspecify.annotations.Nullable;

/**
 * A sequence-generated id whose {@code @GenericGenerator} names {@link ObjectBigIntIdSequenceStyleGenerator} itself, with a non-default sequence name, initial value, increment and optimizer.
 */
@SuppressWarnings("removal") // @GenericGenerator(strategy) is deprecated for removal in Hibernate 7
@Entity
@Table(name = BigIntOwnSequenceGeneratorEntity.TABLE_NAME)
public class BigIntOwnSequenceGeneratorEntity
{

    public static final String TABLE_NAME = "bigint_own_sequence_generator";
    public static final String SEQUENCE_NAME = "bigint_own_custom_seq";
    public static final long INITIAL_VALUE = 40;
    public static final int INCREMENT_SIZE = 7;

    @jakarta.persistence.Id
    @GeneratedValue(generator = "bigint_own_sequence_gen")
    @org.hibernate.annotations.GenericGenerator(
        name = "bigint_own_sequence_gen",
        strategy = "org.framefork.typedIds.bigint.hibernate.id.ObjectBigIntIdSequenceStyleGenerator",
        parameters = {
            @org.hibernate.annotations.Parameter(name = "sequence_name", value = SEQUENCE_NAME),
            @org.hibernate.annotations.Parameter(name = "initial_value", value = "40"),
            @org.hibernate.annotations.Parameter(name = "increment_size", value = "7"),
            @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo")
        }
    )
    @Column(nullable = false)
    @Type(ObjectBigIntIdType.class)
    @Nullable
    private Id id;

    @Column(nullable = false)
    private String title;

    public BigIntOwnSequenceGeneratorEntity(final String title)
    {
        this.title = title;
    }

    @SuppressWarnings("NullAway")
    protected BigIntOwnSequenceGeneratorEntity()
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
