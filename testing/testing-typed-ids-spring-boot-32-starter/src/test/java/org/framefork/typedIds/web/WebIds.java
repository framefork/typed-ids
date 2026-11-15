package org.framefork.typedIds.web;

import org.framefork.typedIds.bigint.ObjectBigIntId;
import org.framefork.typedIds.uuid.ObjectUuid;

import java.util.UUID;

/**
 * Ids without any public factory method, so Spring's own fallback (a static {@code valueOf}/{@code of}/{@code from} taking a String)
 * cannot bind them: only the typed-ids converters can.
 */
public final class WebIds
{

    private WebIds()
    {
    }

    public static final class OrderId extends ObjectUuid<OrderId>
    {

        @SuppressWarnings("UnusedMethod") // called by the typed-ids converters through a MethodHandle
        private OrderId(final UUID inner)
        {
            super(inner);
        }

    }

    public static final class ItemId extends ObjectBigIntId<ItemId>
    {

        @SuppressWarnings("UnusedMethod") // called by the typed-ids converters through a MethodHandle
        private ItemId(final long inner)
        {
            super(inner);
        }

    }

}
