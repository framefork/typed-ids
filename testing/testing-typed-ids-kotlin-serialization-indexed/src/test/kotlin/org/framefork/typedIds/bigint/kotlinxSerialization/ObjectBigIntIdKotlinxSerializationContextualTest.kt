package org.framefork.typedIds.bigint.kotlinxSerialization

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.framefork.typedIds.bigint.ObjectBigIntId
import org.framefork.typedIds.bigint.json.kotlinxSerialization.ObjectBigIntIdKotlinxSerializationModule
import org.junit.jupiter.api.Test

class ObjectBigIntIdKotlinxSerializationContextualTest {

    val json = Json {
        serializersModule = ObjectBigIntIdKotlinxSerializationModule.fromIndex
    }

    @Test
    fun functional() {
        val id = UserEntity.Id.from(42)
        val dto = UserDto(id)

        val dtoJson = json.encodeToString(dto)
        assertThat(dtoJson).isEqualTo("""{"id":42}""")

        val decodedDto = json.decodeFromString<UserDto>(dtoJson)
        assertThat(decodedDto).isEqualTo(dto)
    }

    @Serializable
    data class UserDto(@Contextual val id: UserEntity.Id)

    class UserEntity(val id: Id) {

        class Id private constructor(id: Long) : ObjectBigIntId<Id>(id) {
            companion object {
                fun random() = randomBigInt(::Id)
                fun from(value: String) = fromString(::Id, value)
                fun from(value: Long) = fromLong(::Id, value)
            }
        }

    }

}
