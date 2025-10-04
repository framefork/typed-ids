package org.framefork.typedIds.bigint.hibernate.inheritance.singletable;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TRUCK")
public class BigIntTruck extends BigIntVehicle
{

    @Column
    private Double payloadCapacity;

    @SuppressWarnings({"unused", "NullAway"})
    protected BigIntTruck()
    {
    }

    public BigIntTruck(final String manufacturer, final String model, final Double payloadCapacity)
    {
        super(manufacturer, model);
        this.payloadCapacity = payloadCapacity;
    }

    public Double getPayloadCapacity()
    {
        return payloadCapacity;
    }

    public void setPayloadCapacity(final Double payloadCapacity)
    {
        this.payloadCapacity = payloadCapacity;
    }

}
