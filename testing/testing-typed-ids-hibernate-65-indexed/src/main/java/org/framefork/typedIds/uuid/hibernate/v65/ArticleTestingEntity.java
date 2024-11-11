package org.framefork.typedIds.uuid.hibernate.v65;

import org.framefork.typedIds.uuid.ObjectUuid;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = ArticleTestingEntity.TABLE_NAME)
public class ArticleTestingEntity
{

    public static final String TABLE_NAME = "article";

    @jakarta.persistence.Id
    @Column(nullable = false)
    private Id id;

    @Column(nullable = false)
    private String title;

    public ArticleTestingEntity(final String title)
    {
        this.id = Id.random();
        this.title = title;
    }

    protected ArticleTestingEntity()
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

        public static Id fromString(final String value)
        {
            return ObjectUuid.fromString(Id::new, value);
        }

        public static Id fromUuid(final UUID value)
        {
            return ObjectUuid.fromUuid(Id::new, value);
        }

    }

}
