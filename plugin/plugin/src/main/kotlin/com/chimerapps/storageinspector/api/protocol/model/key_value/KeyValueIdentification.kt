package com.chimerapps.storageinspector.api.protocol.model.key_value

import com.chimerapps.storageinspector.api.protocol.model.StorageType
import com.chimerapps.storageinspector.api.protocol.model.ValueWithType

/**
 * @author Nicola Verbeeck
 */
data class KeyValueServerIdentification(
    val id: String,
    val name: String,
    val icon: String?,
    val keySuggestions: List<ValueWithType>,
    val keyOptions: List<ValueWithType>,
    val supportedKeyTypes: List<StorageType>,
    val supportedValueTypes: List<StorageType>,
    val keyTypeHints: List<KeyTypeHint>,
    val keyIcons: List<KeyIcon>,
)

data class KeyTypeHint(val key: ValueWithType, val type: StorageType)
data class KeyIcon(val key: ValueWithType, val icon: String)