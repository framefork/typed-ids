package org.framefork.typedIds.bigint.hibernate.basic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.hibernate.ObjectBigIntIdType;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.jspecify.annotations.Nullable;

@SuppressWarnings({"removal","UnnecessarilyFullyQualified"})
@Entity
@Table(name = BigIntPooledLoMismatchEntity.TABLE_NAME)
public class BigIntPooledLoMismatchEntity
{

    public static final String TABLE_NAME = "bigint_pooled_lo_mismatch";

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pooled_lo_mismatch_gen")
    @org.hibernate.annotations.GenericGenerator(
        name = "pooled_lo_mismatch_gen",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "pooled_lo_mismatch_seq"),
            @Parameter(name = "initial_value", value = "1"),
            @Parameter(name = "increment_size", value = "20"),
            @Parameter(name = "optimizer", value = "pooled-lo")
        }
    )
    @Column(nullable = false)
    @Type(ObjectBigIntIdType.class)
    @Nullable
    private Id id;

    @Column(nullable = false)
    private String data;

    public BigIntPooledLoMismatchEntity(final String data)
    {
        this.data = data;
    }

    @SuppressWarnings("NullAway")
    protected BigIntPooledLoMismatchEntity()
    {
    }

    @Nullable
    public Id getId()
    {
        return id;
    }

    public String getData()
    {
        return data;
    }

    public static final class Id extends ObjectBigIntId<Id>
    {

        private Id(final long inner)
        {
            super(inner);
        }

        public static Id random()
        {
            return ObjectBigIntId.randomBigInt(Id::new);
        }

        public static Id from(final String value)
        {
            return ObjectBigIntId.fromString(Id::new, value);
        }

        public static Id from(final long value)
        {
            return ObjectBigIntId.fromLong(Id::new, value);
        }

    }

}
