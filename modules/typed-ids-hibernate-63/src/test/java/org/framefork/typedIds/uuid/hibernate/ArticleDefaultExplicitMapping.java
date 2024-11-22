package org.framefork.typedIds.uuid.hibernate;

import org.framefork.typedIds.uuid.ObjectUuid;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = ArticleDefaultExplicitMapping.TABLE_NAME)
public class ArticleDefaultExplicitMapping
{

    public static final String TABLE_NAME = "article_default_explicit_mapping";

    @jakarta.persistence.Id
    @Column(nullable = false)
    @Type(ObjectUuidType.class)
    private Id id;

    @Column(nullable = false)
    private String title;

    public ArticleDefaultExplicitMapping(final String title)
    {
        this.id = Id.random();
        this.title = title;
    }

    @SuppressWarnings("NullAway")
    protected ArticleDefaultExplicitMapping()
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
