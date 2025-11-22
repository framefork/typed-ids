package org.framefork.typedIds.bigint.hibernate.basic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.hibernate.ObjectBigIntIdType;
import org.hibernate.annotations.Type;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = BigIntNullIdEntity.TABLE_NAME)
public class BigIntNullIdEntity
{

    public static final String TABLE_NAME = "bigint_null_id_test";

    @jakarta.persistence.Id
    @Column(nullable = false)
    @Type(ObjectBigIntIdType.class)
    @Nullable
    private Id id;

    @Column(nullable = false)
    private String data;

    public BigIntNullIdEntity(final String data)
    {
        this.data = data;
    }

    @SuppressWarnings("NullAway")
    protected BigIntNullIdEntity()
    {
    }

    @Nullable
    public Id getId()
    {
        return id;
    }

    public void setId(@Nullable final Id id)
    {
        this.id = id;
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
