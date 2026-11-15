package org.framefork.typedIds.bigint.hibernate.inheritance.tableperclass;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class BigIntPublication extends BigIntTablePerClassBaseEntity
{

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String publisher;

    @SuppressWarnings({"unused", "NullAway"})
    protected BigIntPublication()
    {
    }

    public BigIntPublication(final String title, final String publisher)
    {
        this.title = title;
        this.publisher = publisher;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(final String title)
    {
        this.title = title;
    }

    public String getPublisher()
    {
        return publisher;
    }

    public void setPublisher(final String publisher)
    {
        this.publisher = publisher;
    }

}
