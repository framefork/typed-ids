package org.framefork.typedIds.uuid;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.framefork.typedIds.uuid.hibernate.ObjectUuidType;
import org.hibernate.annotations.Type;

import java.util.UUID;

@Entity
@Table(name = UuidEntity.TABLE_NAME)
public class UuidEntity
{

    public static final String TABLE_NAME = "starter_uuid_entity";

    @jakarta.persistence.Id
    @Column(nullable = false)
    @Type(ObjectUuidType.class)
    private Id id;

    @Column(nullable = false)
    private String title;

    public UuidEntity(final String title)
    {
        this.id = Id.random();
        this.title = title;
    }

    @SuppressWarnings("NullAway")
    protected UuidEntity()
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
