package org.framefork.typedIds.bigint.hibernate.inheritance.singletable;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CAR")
public class BigIntCar extends BigIntVehicle
{

    @Column
    private Integer numberOfDoors;

    @SuppressWarnings({"unused", "NullAway"})
    protected BigIntCar()
    {
    }

    public BigIntCar(final String manufacturer, final String model, final Integer numberOfDoors)
    {
        super(manufacturer, model);
        this.numberOfDoors = numberOfDoors;
    }

    public Integer getNumberOfDoors()
    {
        return numberOfDoors;
    }

    public void setNumberOfDoors(final Integer numberOfDoors)
    {
        this.numberOfDoors = numberOfDoors;
    }

}
