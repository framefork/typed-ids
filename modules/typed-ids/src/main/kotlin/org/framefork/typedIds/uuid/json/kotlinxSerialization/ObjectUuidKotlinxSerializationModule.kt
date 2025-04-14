package org.framefork.typedIds.uuid.json.kotlinxSerialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule
import org.framefork.typedIds.TypedIdsRegistry
import org.framefork.typedIds.common.ReflectionHacks
import org.framefork.typedIds.uuid.ObjectUuid
import org.framefork.typedIds.uuid.ObjectUuidTypeUtils
import java.util.*
import java.util.concurrent.ConcurrentHashMap

public object ObjectUuidKotlinxSerializationModule {

    /**
     * Uses [TypedIdsRegistry] to construct the module.
     * This requires the ID types are indexed compile-time.
     */
    public val fromIndex: SerializersModule = SerializersModule {
        for (javaClass in TypedIdsRegistry.getObjectUuidClasses()) {
            @Suppress("UNCHECKED_CAST")
            contextual(javaClass.kotlin, typedIdSerializerProvider(javaClass as Any as Class<out ObjectUuid<*>>))
        }
    }

    private fun <T> typedIdSerializerProvider(javaClass: Class<T>): (typeArgumentsSerializers: List<KSerializer<*>>) -> KSerializer<*> where T : ObjectUuid<T> {
        val fromUuidConstructor = Cache.typedIdConstructor(javaClass)
        return { ObjectUuidSerializer(fromUuidConstructor) }
    }

    private object Cache {

        private val constructorByType = ConcurrentHashMap<String, (UUID) -> ObjectUuid<*>>()

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> typedIdConstructor(javaClass: Class<T>): (UUID) -> T =
            constructorByType.computeIfAbsent(javaClass.name) {
                val mainConstructor = ReflectionHacks.getMainConstructor(javaClass, *arrayOf(UUID::class.java));
                return@computeIfAbsent { raw -> ObjectUuidTypeUtils.wrapUuidToIdentifier(raw, mainConstructor) };
            } as (UUID) -> T

    }

}
