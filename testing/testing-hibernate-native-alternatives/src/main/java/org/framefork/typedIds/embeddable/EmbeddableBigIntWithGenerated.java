package org.framefork.typedIds.embeddable;

import jakarta.persistence.Embeddable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.io.Serializable;

@Embeddable
public record EmbeddableBigIntWithGenerated(
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long value
) implements Serializable
{

    public static EmbeddableBigIntWithGenerated from(long value)
    {
        return new EmbeddableBigIntWithGenerated(value);
    }

    public static EmbeddableBigIntWithGenerated from(String value)
    {
        return new EmbeddableBigIntWithGenerated(Long.parseLong(value));
    }

}
