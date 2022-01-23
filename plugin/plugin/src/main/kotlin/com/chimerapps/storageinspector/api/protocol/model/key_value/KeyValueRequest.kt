package com.chimerapps.storageinspector.api.protocol.model.key_value

import com.chimerapps.storageinspector.api.protocol.model.ValueWithType
import com.google.gsonpackaged.annotations.SerializedName

/**
 * @author Nicola Verbeeck
 */
data class KeyValueRequest(
    val type: KeyValueRequestType,
    val data: KeyValueRequestData?,
)

data class KeyValueRequestData(
    val id: String,
    val key: ValueWithType? = null,
    val value: ValueWithType? = null,
)

enum class KeyValueRequestType {
    @SerializedName("get")
    GET,
    @SerializedName("set")
    SET,
    @SerializedName("clear")
    CLEAR,
    @SerializedName("remove")
    REMOVE,
    @SerializedName("get_value")
    GET_VALUE,
}