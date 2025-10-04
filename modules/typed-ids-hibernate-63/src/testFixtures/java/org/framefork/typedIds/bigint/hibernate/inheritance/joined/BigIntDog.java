package org.framefork.typedIds.bigint.hibernate.inheritance.joined;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = BigIntDog.TABLE_NAME)
public class BigIntDog extends BigIntAnimal
{

    public static final String TABLE_NAME = "bigint_joined_dog";

    @Column
    private String breed;

    @SuppressWarnings({"unused", "NullAway"})
    protected BigIntDog()
    {
    }

    public BigIntDog(final String name, final String breed)
    {
        super("Canis lupus familiaris", name);
        this.breed = breed;
    }

    public String getBreed()
    {
        return breed;
    }

    public void setBreed(final String breed)
    {
        this.breed = breed;
    }

}
