package org.framefork.typedIds.uuid;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class ObjectUuidWithoutHibernateTypesTest
{

    @Test
    public void usage()
    {
        var id = UserId.fromString("33a7641c-811e-40b7-986e-ad109cfcf220");
        Assertions.assertEquals("33a7641c-811e-4000b7-986e-ad109cfcf220", id.toString());
    }

}
