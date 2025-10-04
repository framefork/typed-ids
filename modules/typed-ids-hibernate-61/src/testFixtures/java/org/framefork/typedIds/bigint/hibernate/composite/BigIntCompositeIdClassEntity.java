package org.framefork.typedIds.bigint.hibernate.composite;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import org.framefork.typedIds.bigint.hibernate.ObjectBigIntIdType;
import org.hibernate.annotations.Type;

@Entity
@Table(name = BigIntCompositeIdClassEntity.TABLE_NAME)
@IdClass(BigIntIdClassPK.class)
public class BigIntCompositeIdClassEntity
{

    public static final String TABLE_NAME = "bigint_composite_idclass_entity";

    @jakarta.persistence.Id
    @Type(ObjectBigIntIdType.class)
    @Column(name = "custom_id_part")
    private BigIntIdClassPK.Id customIdPart;

    @jakarta.persistence.Id
    @Column(name = "string_part")
    private String stringPart;

    @Column(nullable = false)
    private String data;

    public BigIntCompositeIdClassEntity(final BigIntIdClassPK.Id customIdPart, final String stringPart, final String data)
    {
        this.customIdPart = customIdPart;
        this.stringPart = stringPart;
        this.data = data;
    }

    @SuppressWarnings("NullAway")
    protected BigIntCompositeIdClassEntity()
    {
    }

    public BigIntIdClassPK.Id getCustomIdPart()
    {
        return customIdPart;
    }

    public String getStringPart()
    {
        return stringPart;
    }

    public String getData()
    {
        return data;
    }

}
