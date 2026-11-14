package org.framefork.typedIds.bigint.json.kotlinxSerialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import org.framefork.typedIds.TypedIdsRegistry
import org.framefork.typedIds.bigint.ObjectBigIntId
import org.framefork.typedIds.bigint.ObjectBigIntIdTypeUtils
import org.framefork.typedIds.common.ReflectionHacks
import java.util.concurrent.ConcurrentHashMap

public object ObjectBigIntIdKotlinxSerializationModule {

    /**
     * Uses [TypedIdsRegistry] to construct the module.
     * This requires the ID types are indexed compile-time.
     */
    public val fromIndex: SerializersModule = SerializersModule {
        for (javaClass in TypedIdsRegistry.getObjectBigIntIdClasses()) {
            @Suppress("UNCHECKED_CAST")
            contextual(javaClass.kotlin, typedIdSerializerProvider(javaClass as Any as Class<out ObjectBigIntId<*>>))
        }
    }

    private fun <T> typedIdSerializerProvider(javaClass: Class<T>): (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*> where T : ObjectBigIntId<T> {
        val fromLongConstructor = Cache.typedIdConstructor(javaClass)
        return { ObjectBigIntIdSerializer(fromLongConstructor) }
    }

    private object Cache {

        private val constructorByType = ConcurrentHashMap<String, (Long) -> ObjectBigIntId<*>>()

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> typedIdConstructor(javaClass: Class<T>): (Long) -> T =
            constructorByType.computeIfAbsent(javaClass.name) {
                val mainConstructor = ReflectionHacks.getConstructor(javaClass, *arrayOf(Long::class.java));
                return@computeIfAbsent { raw -> ObjectBigIntIdTypeUtils.wrapBigIntToIdentifier(raw, mainConstructor) };
            } as (Long) -> T

    }

}
