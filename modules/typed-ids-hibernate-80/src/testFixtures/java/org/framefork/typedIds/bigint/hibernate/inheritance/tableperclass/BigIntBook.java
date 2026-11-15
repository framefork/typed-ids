package org.framefork.typedIds.bigint.hibernate.inheritance.tableperclass;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = BigIntBook.TABLE_NAME)
public class BigIntBook extends BigIntPublication
{

    public static final String TABLE_NAME = "bigint_tableperclass_book";

    @Column
    private String isbn;

    @Column
    private Integer pageCount;

    @SuppressWarnings({"unused", "NullAway"})
    protected BigIntBook()
    {
    }

    public BigIntBook(final String title, final String publisher, final String isbn, final Integer pageCount)
    {
        super(title, publisher);
        this.isbn = isbn;
        this.pageCount = pageCount;
    }

    public String getIsbn()
    {
        return isbn;
    }

    public void setIsbn(final String isbn)
    {
        this.isbn = isbn;
    }

    public Integer getPageCount()
    {
        return pageCount;
    }

    public void setPageCount(final Integer pageCount)
    {
        this.pageCount = pageCount;
    }

}
