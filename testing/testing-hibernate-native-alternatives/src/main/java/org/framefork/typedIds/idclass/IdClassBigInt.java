package org.framefork.typedIds.idclass;

import java.io.Serializable;

public record IdClassBigInt(
    Long value
) implements Serializable
{

    public static IdClassBigInt from(long value)
    {
        return new IdClassBigInt(value);
    }

    public static IdClassBigInt from(String value)
    {
        return new IdClassBigInt(Long.parseLong(value));
    }

}
