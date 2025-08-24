package org.framefork.typedIds.idclass;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = IdClassBigIntEntityGeneratedId.TABLE_NAME)
@IdClass(IdClassBigInt.class)
public class IdClassBigIntEntityGeneratedId
{

    public static final String TABLE_NAME = "id_class_generated_id";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "value", nullable = false)
    @Nullable
    private Long value;

    @Column(nullable = false)
    private String name;

    public IdClassBigIntEntityGeneratedId(String name)
    {
        this.name = name;
    }

    @SuppressWarnings("NullAway")
    protected IdClassBigIntEntityGeneratedId()
    {
    }

    @Nullable
    public IdClassBigInt getId()
    {
        return value != null ? new IdClassBigInt(value) : null;
    }

    @Nullable
    public Long getValue()
    {
        return value;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

}
