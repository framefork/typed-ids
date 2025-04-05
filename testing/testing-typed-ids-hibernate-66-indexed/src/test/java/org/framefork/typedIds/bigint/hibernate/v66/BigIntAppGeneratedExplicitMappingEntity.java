package org.framefork.typedIds.bigint.hibernate.v66;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.framefork.typedIds.bigint.ObjectBigIntId;

@Entity
@Table(name = BigIntAppGeneratedExplicitMappingEntity.TABLE_NAME)
public class BigIntAppGeneratedExplicitMappingEntity
{

    public static final String TABLE_NAME = "bigint_app_generated_explicit_mapping";

    @jakarta.persistence.Id
    @Column(nullable = false)
    private Id id;

    @Column(nullable = false)
    private String title;

    public BigIntAppGeneratedExplicitMappingEntity(final String title)
    {
        this.id = Id.random();
        this.title = title;
    }

    @SuppressWarnings("NullAway")
    protected BigIntAppGeneratedExplicitMappingEntity()
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
