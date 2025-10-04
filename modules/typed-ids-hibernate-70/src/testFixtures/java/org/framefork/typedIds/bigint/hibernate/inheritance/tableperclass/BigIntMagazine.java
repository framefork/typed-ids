package org.framefork.typedIds.bigint.hibernate.inheritance.tableperclass;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = BigIntMagazine.TABLE_NAME)
public class BigIntMagazine extends BigIntPublication
{

    public static final String TABLE_NAME = "bigint_tableperclass_magazine";

    @Column
    private Integer issueNumber;

    @Column
    private String frequency;

    @SuppressWarnings({"unused", "NullAway"})
    protected BigIntMagazine()
    {
    }

    public BigIntMagazine(final String title, final String publisher, final Integer issueNumber, final String frequency)
    {
        super(title, publisher);
        this.issueNumber = issueNumber;
        this.frequency = frequency;
    }

    public Integer getIssueNumber()
    {
        return issueNumber;
    }

    public void setIssueNumber(final Integer issueNumber)
    {
        this.issueNumber = issueNumber;
    }

    public String getFrequency()
    {
        return frequency;
    }

    public void setFrequency(final String frequency)
    {
        this.frequency = frequency;
    }

}
