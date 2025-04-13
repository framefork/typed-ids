package org.framefork.typedIds.uuid.json.kotlinxSerialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.framefork.typedIds.uuid.ObjectUuid
import java.util.*

open class ObjectUuidSerializer<T : ObjectUuid<*>>(
    private val fromUuidConstructor: (UUID) -> T,
) : KSerializer<T> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ObjectUuid", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeString(value.toString());
    }

    override fun deserialize(decoder: Decoder): T {
        val rawUuid = UUID.fromString(decoder.decodeString())
        return fromUuidConstructor.invoke(rawUuid)
    }

}
