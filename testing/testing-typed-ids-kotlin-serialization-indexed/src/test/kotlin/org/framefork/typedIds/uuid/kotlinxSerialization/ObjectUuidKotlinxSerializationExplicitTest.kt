package org.framefork.typedIds.uuid.kotlinxSerialization

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.framefork.typedIds.uuid.ObjectUuid
import org.framefork.typedIds.uuid.json.kotlinxSerialization.ObjectUuidSerializer
import org.junit.jupiter.api.Test
import java.util.*

class ObjectUuidKotlinxSerializationExplicitTest {

    val json = Json.Default

    @Test
    fun functional() {
        val id = UserEntity.Id.from("33a7641c-811e-40b7-986e-ad109cfcf220")
        val dto = UserDto(id)

        val dtoJson = json.encodeToString(dto)
        assertThat(dtoJson).isEqualTo("""{"id":"33a7641c-811e-40b7-986e-ad109cfcf220"}""")

        val decodedDto = json.decodeFromString<UserDto>(dtoJson)
        assertThat(decodedDto).isEqualTo(dto)
    }

    @Serializable
    data class UserDto(val id: UserEntity.Id)

    class UserEntity(val id: Id) {

        @Serializable(with = Id.Serializer::class)
        class Id private constructor(id: UUID) : ObjectUuid<Id>(id) {
            companion object {
                fun random() = randomUUID(::Id)
                fun from(value: String) = fromString(::Id, value)
                fun from(value: UUID) = fromUuid(::Id, value)
            }

            object Serializer : ObjectUuidSerializer<Id>(::Id)
        }

    }

}
