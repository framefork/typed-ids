package org.framefork.typedIds.bigint.hibernate.relationships;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = BigIntChildEntity.TABLE_NAME)
public class BigIntChildEntity
{

    public static final String TABLE_NAME = "bigint_rel_child_entity";

    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false)
    private String data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @Nullable
    private BigIntParentEntity parent;

    @SuppressWarnings("NullAway")
    public BigIntChildEntity(final String data)
    {
        this.data = data;
    }

    @SuppressWarnings("NullAway")
    protected BigIntChildEntity()
    {
    }

    public Long getId()
    {
        return id;
    }

    public String getData()
    {
        return data;
    }

    @Nullable
    public BigIntParentEntity getParent()
    {
        return parent;
    }

    public void setParent(@Nullable final BigIntParentEntity parent)
    {
        this.parent = parent;
    }

}
