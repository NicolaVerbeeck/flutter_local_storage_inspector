package com.chimerapps.storageinspector.api.protocol.model

/**
 * @author Nicola Verbeeck
 */
data class ValueWithType(val type: StorageType, val value: Any)

enum class StorageType {
    string,
    integer,
    double,
    datetime,
    binary,
    boolean,
    stringList,
}