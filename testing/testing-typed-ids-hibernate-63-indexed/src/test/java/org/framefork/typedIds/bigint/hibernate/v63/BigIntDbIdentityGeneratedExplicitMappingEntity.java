package org.framefork.typedIds.bigint.hibernate.v63;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = BigIntDbIdentityGeneratedExplicitMappingEntity.TABLE_NAME)
public class BigIntDbIdentityGeneratedExplicitMappingEntity
{

    public static final String TABLE_NAME = "bigint_db_identity_generated_explicit_mapping";

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    @Nullable
    private Id id;

    @Column(nullable = false)
    private String title;

    public BigIntDbIdentityGeneratedExplicitMappingEntity(final String title)
    {
        this.title = title;
    }

    @SuppressWarnings("NullAway")
    protected BigIntDbIdentityGeneratedExplicitMappingEntity()
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
