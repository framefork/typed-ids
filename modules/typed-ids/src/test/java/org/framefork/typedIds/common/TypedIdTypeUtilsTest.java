package org.framefork.typedIds.common;

import org.framefork.typedIds.bigint.ObjectBigIntIdMock;
import org.framefork.typedIds.uuid.ObjectUuidMock;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

final class TypedIdTypeUtilsTest
{

    @Test
    void dispatchesByIdType()
    {
        assertThat(TypedIdTypeUtils.convert(ObjectUuidMock.class, "01936d8a-7b5e-7c3f-9a1b-2c3d4e5f6a7b")).isEqualTo(ObjectUuidMock.from("01936d8a-7b5e-7c3f-9a1b-2c3d4e5f6a7b"));
        assertThat(TypedIdTypeUtils.convert(ObjectBigIntIdMock.class, 42)).isEqualTo(ObjectBigIntIdMock.from(42L));
    }

    @Test
    void rejectsOtherTypes()
    {
        assertThatThrownBy(() -> TypedIdTypeUtils.convert(String.class, "42"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(String.class.getName());
    }

}
