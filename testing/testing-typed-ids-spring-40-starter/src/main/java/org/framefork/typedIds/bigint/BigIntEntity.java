package org.framefork.typedIds.bigint;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.framefork.typedIds.bigint.hibernate.ObjectBigIntIdType;
import org.hibernate.annotations.Type;

@Entity
@Table(name = BigIntEntity.TABLE_NAME)
public class BigIntEntity
{

    public static final String TABLE_NAME = "starter_bigint_entity";

    @jakarta.persistence.Id
    @Column(nullable = false)
    @Type(ObjectBigIntIdType.class)
    private Id id;

    @Column(nullable = false)
    private String title;

    public BigIntEntity(final String title)
    {
        this.id = Id.random();
        this.title = title;
    }

    @SuppressWarnings("NullAway")
    protected BigIntEntity()
    {
    }

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
