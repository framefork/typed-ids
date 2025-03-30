package org.framefork.typedIds.uuid.hibernate.v66;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.framefork.typedIds.uuid.ObjectUuid;

import java.util.UUID;

@Entity
@Table(name = UuidAppGeneratedExplicitMappingEntity.TABLE_NAME)
public class UuidAppGeneratedExplicitMappingEntity
{

    public static final String TABLE_NAME = "uuid_app_generated_explicit_mapping";

    @jakarta.persistence.Id
    @Column(nullable = false)
    private Id id;

    @Column(nullable = false)
    private String title;

    public UuidAppGeneratedExplicitMappingEntity(final String title)
    {
        this.id = Id.random();
        this.title = title;
    }

    @SuppressWarnings("NullAway")
    protected UuidAppGeneratedExplicitMappingEntity()
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
