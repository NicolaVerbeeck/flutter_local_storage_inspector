package com.chimerapps.storageinspector.api.protocol.model.key_value

import com.chimerapps.storageinspector.api.protocol.model.ValueWithType

/**
 * @author Nicola Verbeeck
 */
data class KeyValueServerValues(
    val id: String,
    val values: List<KeyValueServerValue>,
)

data class KeyValueServerValue(
    val key: ValueWithType,
    val value: ValueWithType,
)