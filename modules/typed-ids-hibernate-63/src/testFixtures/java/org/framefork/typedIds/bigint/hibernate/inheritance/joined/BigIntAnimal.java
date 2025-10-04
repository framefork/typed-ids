package org.framefork.typedIds.bigint.hibernate.inheritance.joined;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import org.framefork.typedIds.bigint.hibernate.inheritance.BigIntBaseEntity;

@Entity
@Table(name = BigIntAnimal.TABLE_NAME)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class BigIntAnimal extends BigIntBaseEntity
{

    public static final String TABLE_NAME = "bigint_joined_animal";

    @Column(nullable = false)
    private String species;

    @Column(nullable = false)
    private String name;

    @SuppressWarnings({"unused", "NullAway"})
    protected BigIntAnimal()
    {
    }

    public BigIntAnimal(final String species, final String name)
    {
        this.species = species;
        this.name = name;
    }

    public String getSpecies()
    {
        return species;
    }

    public void setSpecies(final String species)
    {
        this.species = species;
    }

    public String getName()
    {
        return name;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

}
