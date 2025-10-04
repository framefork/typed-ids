package org.framefork.typedIds.uuid.hibernate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.framefork.typedIds.uuid.ObjectUuid;
import org.hibernate.annotations.Type;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

@Entity
@Table(name = UuidNullIdEntity.TABLE_NAME)
public class UuidNullIdEntity
{

    public static final String TABLE_NAME = "uuid_null_id_test";

    @jakarta.persistence.Id
    @Column(nullable = false)
    @Type(ObjectUuidType.class)
    @Nullable
    private Id id;

    @Column(nullable = false)
    private String data;

    public UuidNullIdEntity(final String data)
    {
        this.data = data;
    }

    @SuppressWarnings("NullAway")
    protected UuidNullIdEntity()
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

    public static final class Id extends ObjectUuid<Id>
    {

        private Id(final UUID inner)
        {
            super(inner);
        }

        public static Id random()
        {
            return ObjectUuid.randomUUID(Id::new);
        }

        public static Id from(final String value)
        {
            return ObjectUuid.fromString(Id::new, value);
        }

        public static Id from(final UUID value)
        {
            return ObjectUuid.fromUuid(Id::new, value);
        }

    }

}
