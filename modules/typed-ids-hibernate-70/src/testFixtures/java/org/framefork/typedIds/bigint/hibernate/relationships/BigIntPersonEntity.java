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
@Table(name = BigIntPersonEntity.TABLE_NAME)
public class BigIntPersonEntity
{

    public static final String TABLE_NAME = "bigint_rel_person_entity";

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    @Type(ObjectBigIntIdType.class)
    @Nullable
    private Id id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    public BigIntPersonEntity(final String firstName, final String lastName)
    {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @SuppressWarnings("NullAway")
    protected BigIntPersonEntity()
    {
    }

    @Nullable
    public Id getId()
    {
        return id;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
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
