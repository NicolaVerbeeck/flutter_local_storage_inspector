package com.chimerapps.storageinspector.api.protocol.model

/**
 * @author Nicola Verbeeck
 */
data class ServerId(
    val bundleId: String,
    val version: Int,
    val icon: String?,
)