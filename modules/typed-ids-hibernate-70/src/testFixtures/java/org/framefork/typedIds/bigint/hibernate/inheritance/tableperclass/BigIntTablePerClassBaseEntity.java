package org.framefork.typedIds.bigint.hibernate.inheritance.tableperclass;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.MappedSuperclass;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.hibernate.ObjectBigIntIdType;
import org.hibernate.annotations.Type;
import org.jspecify.annotations.Nullable;

@MappedSuperclass
public abstract class BigIntTablePerClassBaseEntity
{

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    @Type(ObjectBigIntIdType.class)
    @Nullable
    protected Id id;

    @Column(nullable = false)
    protected String createdBy = "system";

    @SuppressWarnings({"unused", "NullAway"})
    protected BigIntTablePerClassBaseEntity()
    {
    }

    @Nullable
    public Id getId()
    {
        return id;
    }

    public String getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(final String createdBy)
    {
        this.createdBy = createdBy;
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
