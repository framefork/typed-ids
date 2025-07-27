package org.framefork.typedIds.bigint.hibernate.composite;

import jakarta.persistence.Embeddable;
import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.bigint.hibernate.ObjectBigIntIdType;
import org.hibernate.annotations.Type;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class BigIntCompositePK implements Serializable
{

    @Type(ObjectBigIntIdType.class)
    private Id customIdPart;

    private String stringPart;

    @SuppressWarnings({"unused", "NullAway"})
    protected BigIntCompositePK()
    {
    }

    public BigIntCompositePK(final Id customIdPart, final String stringPart)
    {
        this.customIdPart = customIdPart;
        this.stringPart = stringPart;
    }

    public Id getCustomIdPart()
    {
        return customIdPart;
    }

    public void setCustomIdPart(final Id customIdPart)
    {
        this.customIdPart = customIdPart;
    }

    public String getStringPart()
    {
        return stringPart;
    }

    public void setStringPart(final String stringPart)
    {
        this.stringPart = stringPart;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (!(o instanceof BigIntCompositePK that)) {
            return false;
        }
        return Objects.equals(customIdPart, that.customIdPart)
            && Objects.equals(stringPart, that.stringPart);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(customIdPart, stringPart);
    }

    @Override
    public String toString()
    {
        return "CompositePK{" +
            "customIdPart=" + customIdPart +
            ", stringPart='" + stringPart + '\'' +
            '}';
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
