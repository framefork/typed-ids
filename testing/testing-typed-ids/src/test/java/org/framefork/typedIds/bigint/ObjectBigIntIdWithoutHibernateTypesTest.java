package org.framefork.typedIds.bigint;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class ObjectBigIntIdWithoutHibernateTypesTest
{

    @Test
    public void usage()
    {
        var id = OrderId.from("693968158089838711");
        Assertions.assertEquals("693968158089838711", id.toString());
    }

}
