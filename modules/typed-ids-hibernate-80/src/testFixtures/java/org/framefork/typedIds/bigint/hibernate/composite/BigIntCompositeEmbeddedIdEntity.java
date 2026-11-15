package org.framefork.typedIds.bigint.hibernate.composite;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = BigIntCompositeEmbeddedIdEntity.TABLE_NAME)
public class BigIntCompositeEmbeddedIdEntity
{

    public static final String TABLE_NAME = "bigint_composite_embedded_id_entity";

    @EmbeddedId
    private BigIntCompositePK id;

    @Column(nullable = false)
    private String data;

    public BigIntCompositeEmbeddedIdEntity(final BigIntCompositePK id, final String data)
    {
        this.id = id;
        this.data = data;
    }

    @SuppressWarnings("NullAway")
    protected BigIntCompositeEmbeddedIdEntity()
    {
    }

    public BigIntCompositePK getId()
    {
        return id;
    }

    public String getData()
    {
        return data;
    }

}
