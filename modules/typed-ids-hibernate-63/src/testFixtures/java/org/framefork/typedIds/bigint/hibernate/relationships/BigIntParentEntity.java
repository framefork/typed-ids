package org.framefork.typedIds.bigint.hibernate.relationships;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.hibernate.ObjectBigIntIdType;
import org.hibernate.annotations.Type;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = BigIntParentEntity.TABLE_NAME)
public class BigIntParentEntity
{

    public static final String TABLE_NAME = "bigint_rel_parent_entity";

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    @Type(ObjectBigIntIdType.class)
    @Nullable
    private Id id;

    @Column(nullable = false)
    private String name;

    public BigIntParentEntity(final String name)
    {
        this.name = name;
    }

    @SuppressWarnings("NullAway")
    protected BigIntParentEntity()
    {
    }

    @Nullable
    public Id getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
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
