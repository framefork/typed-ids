package org.framefork.typedIds.bigint.hibernate.inheritance.singletable;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import org.framefork.typedIds.bigint.hibernate.inheritance.BigIntBaseEntity;

@Entity
@Table(name = BigIntVehicle.TABLE_NAME)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "vehicle_type")
public abstract class BigIntVehicle extends BigIntBaseEntity
{

    public static final String TABLE_NAME = "bigint_single_table_vehicle";

    @Column(nullable = false)
    private String manufacturer;

    @Column(nullable = false)
    private String model;

    @SuppressWarnings({"unused", "NullAway"})
    protected BigIntVehicle()
    {
    }

    public BigIntVehicle(final String manufacturer, final String model)
    {
        this.manufacturer = manufacturer;
        this.model = model;
    }

    public String getManufacturer()
    {
        return manufacturer;
    }

    public void setManufacturer(final String manufacturer)
    {
        this.manufacturer = manufacturer;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(final String model)
    {
        this.model = model;
    }

}
