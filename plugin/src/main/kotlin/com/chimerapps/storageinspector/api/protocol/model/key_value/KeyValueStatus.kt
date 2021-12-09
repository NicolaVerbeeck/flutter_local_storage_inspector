package com.chimerapps.storageinspector.api.protocol.model.key_value

/**
 * @author Nicola Verbeeck
 */
data class KeyValueServerStatus(val id: String, val data: KeyValueStatus)

data class KeyValueStatus(val success: Boolean)