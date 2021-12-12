package com.chimerapps.storageinspector.api.protocol.model

import com.google.gsonpackaged.JsonArray
import com.google.gsonpackaged.JsonDeserializationContext
import com.google.gsonpackaged.JsonDeserializer
import com.google.gsonpackaged.JsonElement
import com.google.gsonpackaged.JsonObject
import com.google.gsonpackaged.JsonSerializationContext
import com.google.gsonpackaged.JsonSerializer
import java.lang.reflect.Type

/**
 * @author Nicola Verbeeck
 */
data class ValueWithType(val type: StorageType, val value: Any)

enum class StorageType {
    string,
    int,
    double,
    datetime,
    binary,
    bool,
    stringlist,
}

class ValueWithTypeTypeAdapter : JsonDeserializer<ValueWithType>, JsonSerializer<ValueWithType> {
    @Suppress("IMPLICIT_CAST_TO_ANY")
    override fun deserialize(element: JsonElement, type: Type?, context: JsonDeserializationContext): ValueWithType {
        val typeObject = element.asJsonObject

        val storageType = context.deserialize<StorageType>(typeObject.get("type"), StorageType::class.java)!!
        val value = when (storageType) {
            StorageType.string -> typeObject.get("value").asString
            StorageType.int -> typeObject.get("value").asLong
            StorageType.double -> typeObject.get("value").asDouble
            StorageType.datetime -> typeObject.get("value").asLong
            StorageType.binary -> typeObject.get("value").asString
            StorageType.bool -> typeObject.get("value").asBoolean
            StorageType.stringlist -> typeObject.get("value").asJsonArray.map { it.asString }
        }

        return ValueWithType(storageType, value)
    }

    override fun serialize(value: ValueWithType, type: Type, context: JsonSerializationContext): JsonElement {
        val asJson = JsonObject()

        asJson.add("type", context.serialize(value.type))

        when (value.type) {
            StorageType.string -> asJson.addProperty("value", value.value as String)
            StorageType.int -> asJson.addProperty("value", (value.value as Number).toLong())
            StorageType.double -> asJson.addProperty("value", (value.value as Number).toDouble())
            StorageType.datetime -> asJson.addProperty("value", (value.value as Number).toLong())
            StorageType.binary -> asJson.addProperty("value", value.value as String)
            StorageType.bool -> asJson.addProperty("value", value.value as Boolean)
            StorageType.stringlist -> asJson.add("value", JsonArray().also {
                (value.value as List<*>).forEach { element -> it.add(element.toString()) }
            })
        }

        return asJson
    }


}