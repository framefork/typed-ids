package org.framefork.typedIds.uuid;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class ObjectUuidTypeUtilsTest
{

    private static final String UUID_V7 = "01936d8a-7b5e-7c3f-9a1b-2c3d4e5f6a7b";
    private static final String UUID_V1 = "550e8400-e29b-11d4-a716-446655440000";

    @Test
    void convertsUuidAndCanonicalString()
    {
        assertThat(ObjectUuidTypeUtils.convert(ObjectUuidMock.class, UUID.fromString(UUID_V7))).isEqualTo(ObjectUuidMock.from(UUID_V7));
        assertThat(ObjectUuidTypeUtils.convert(ObjectUuidMock.class, UUID_V7)).isEqualTo(ObjectUuidMock.from(UUID_V7));
        assertThat(ObjectUuidTypeUtils.convert(ObjectUuidMock.class, " " + UUID_V7 + " ")).isEqualTo(ObjectUuidMock.from(UUID_V7));
    }

    @Test
    void rejectsMalformedInputAsIllegalArgument()
    {
        assertThatThrownBy(() -> ObjectUuidTypeUtils.convert(ObjectUuidMock.class, "not-a-uuid"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Cannot convert \"not-a-uuid\" to " + ObjectUuidMock.class.getName());
        assertThatThrownBy(() -> ObjectUuidTypeUtils.convert(ObjectUuidMock.class, "1-1-1-1-1"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ObjectUuidTypeUtils.convert(ObjectUuidMock.class, " "))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ObjectUuidTypeUtils.convert(ObjectUuidMock.class, 42L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(Long.class.getName());
    }

    @Test
    void rejectsUnsupportedUuidVersionAsIllegalArgument()
    {
        assertThatThrownBy(() -> ObjectUuidTypeUtils.convert(ObjectUuidMock.class, UUID_V1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Only versions");
        assertThatThrownBy(() -> ObjectUuidTypeUtils.convert(ObjectUuidMock.class, UUID.fromString(UUID_V1)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsIdTypeWithoutConstructorAsIllegalState()
    {
        assertThatThrownBy(() -> ObjectUuidTypeUtils.getMainConstructor(InvalidUuidId.class))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining(InvalidUuidId.class.getName());
        assertThatThrownBy(() -> ObjectUuidTypeUtils.convert(InvalidUuidId.class, UUID_V7))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cachesTheConstructorPerType()
    {
        assertThat(ObjectUuidTypeUtils.getMainConstructor(ObjectUuidMock.class)).isSameAs(ObjectUuidTypeUtils.getMainConstructor(ObjectUuidMock.class));
    }

    /**
     * Lacks the private UUID constructor every typed id has.
     */
    static final class InvalidUuidId extends ObjectUuid<InvalidUuidId>
    {

        @SuppressWarnings({"UnusedMethod", "UnusedVariable"})
        private InvalidUuidId(final String invalid)
        {
            super(UUID.fromString(UUID_V7));
        }

    }

}
