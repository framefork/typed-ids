package org.framefork.typedIds.bigint.json.kotlinxSerialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.framefork.typedIds.bigint.ObjectBigIntId

open class ObjectBigIntIdSerializer<T : ObjectBigIntId<*>>(
    private val fromLongConstructor: (Long) -> T,
) : KSerializer<T> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ObjectBigIntId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeLong(value.toLong());
    }

    override fun deserialize(decoder: Decoder): T {
        val rawLong = decoder.decodeLong()
        return fromLongConstructor.invoke(rawLong)
    }

}
