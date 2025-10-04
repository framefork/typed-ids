package org.framefork.typedIds.bigint.hibernate.relationships;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.framefork.typedIds.bigint.hibernate.ObjectBigIntIdType;
import org.hibernate.annotations.Type;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = BigIntPersonDetailsEntity.TABLE_NAME)
public class BigIntPersonDetailsEntity
{

    public static final String TABLE_NAME = "bigint_rel_person_details_entity";

    @jakarta.persistence.Id
    @Column(nullable = false)
    @Type(ObjectBigIntIdType.class)
    private BigIntPersonEntity.@Nullable Id id;

    @Column(nullable = false)
    private String email;

    @Column
    @Nullable
    private String phoneNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    @Nullable
    private BigIntPersonEntity person;

    public BigIntPersonDetailsEntity(final String email, @Nullable final String phoneNumber)
    {
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    @SuppressWarnings("NullAway")
    protected BigIntPersonDetailsEntity()
    {
    }

    public BigIntPersonEntity.@Nullable Id getId()
    {
        return id;
    }

    public String getEmail()
    {
        return email;
    }

    @Nullable
    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    @Nullable
    public BigIntPersonEntity getPerson()
    {
        return person;
    }

    public void setPerson(@Nullable final BigIntPersonEntity person)
    {
        this.person = person;
    }

}
