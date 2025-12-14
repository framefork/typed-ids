package org.framefork.typedIds.bigint.hibernate.inheritance.joined;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = BigIntCat.TABLE_NAME)
public class BigIntCat extends BigIntAnimal
{

    public static final String TABLE_NAME = "bigint_joined_cat";

    @Column
    private Boolean isIndoor;

    @SuppressWarnings({"unused", "NullAway"})
    protected BigIntCat()
    {
    }

    public BigIntCat(final String name, final Boolean isIndoor)
    {
        super("Felis catus", name);
        this.isIndoor = isIndoor;
    }

    public Boolean getIsIndoor()
    {
        return isIndoor;
    }

    public void setIsIndoor(final Boolean isIndoor)
    {
        this.isIndoor = isIndoor;
    }

}
