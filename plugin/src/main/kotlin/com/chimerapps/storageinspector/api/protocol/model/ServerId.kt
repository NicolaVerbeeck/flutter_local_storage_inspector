package com.chimerapps.storageinspector.api.protocol.model

/**
 * @author Nicola Verbeeck
 */
data class ServerId(
    val bundle: String,
    val version: Int,
    val icon: String?,
)