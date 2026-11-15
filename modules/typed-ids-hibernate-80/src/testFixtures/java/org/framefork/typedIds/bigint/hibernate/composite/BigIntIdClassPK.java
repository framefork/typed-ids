package org.framefork.typedIds.bigint.hibernate.composite;

import org.framefork.typedIds.bigint.ObjectBigIntId;

import java.io.Serializable;
import java.util.Objects;

/**
 * Separate ID class for @IdClass approach (not @Embeddable)
 */
public class BigIntIdClassPK implements Serializable
{

    private Id customIdPart;
    private String stringPart;

    @SuppressWarnings({"unused", "NullAway"})
    protected BigIntIdClassPK()
    {
    }

    public BigIntIdClassPK(final Id customIdPart, final String stringPart)
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
        if (!(o instanceof BigIntIdClassPK idClassPK)) {
            return false;
        }
        return Objects.equals(customIdPart, idClassPK.customIdPart)
            && Objects.equals(stringPart, idClassPK.stringPart);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(customIdPart, stringPart);
    }

    @Override
    public String toString()
    {
        return "IdClassPK{" +
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
