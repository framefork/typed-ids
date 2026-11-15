package org.framefork.typedIds.bigint.hibernate.basic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.hibernate.ObjectBigIntIdType;
import org.hibernate.annotations.Type;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.jspecify.annotations.Nullable;

/**
 * A sequence-generated id with a non-default sequence name, initial value, increment and optimizer from {@code @GenericGenerator}.
 */
@Entity
@Table(name = BigIntGenericSequenceGeneratorEntity.TABLE_NAME)
public class BigIntGenericSequenceGeneratorEntity
{

    public static final String TABLE_NAME = "bigint_generic_sequence_generator";
    public static final String SEQUENCE_NAME = "bigint_generic_custom_seq";
    public static final long INITIAL_VALUE = 100;
    public static final int INCREMENT_SIZE = 7;

    @jakarta.persistence.Id
    @org.hibernate.annotations.GenericGenerator(
        type = SequenceStyleGenerator.class,
        parameters = {
            @org.hibernate.annotations.Parameter(name = "sequence_name", value = SEQUENCE_NAME),
            @org.hibernate.annotations.Parameter(name = "initial_value", value = "100"),
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

    public BigIntGenericSequenceGeneratorEntity(final String title)
    {
        this.title = title;
    }

    @SuppressWarnings("NullAway")
    protected BigIntGenericSequenceGeneratorEntity()
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
