package org.framefork.typedIds.bigint.kotlinxSerialization

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.framefork.typedIds.bigint.ObjectBigIntId
import org.framefork.typedIds.bigint.json.kotlinxSerialization.ObjectBigIntIdSerializer
import org.junit.jupiter.api.Test

class ObjectBigIntIdKotlinxSerializationExplicitTest {

    val json = Json.Default

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
    data class UserDto(val id: UserEntity.Id)

    class UserEntity(val id: Id) {

        @Serializable(with = Id.Serializer::class)
        class Id private constructor(id: Long) : ObjectBigIntId<Id>(id) {
            companion object {
                fun random() = randomBigInt(::Id)
                fun from(value: String) = fromString(::Id, value)
                fun from(value: Long) = fromLong(::Id, value)
            }

            object Serializer : ObjectBigIntIdSerializer<Id>(::Id)
        }

    }

}
