package com.chimerapps.storageinspector.api.protocol.model.sql

import com.chimerapps.storageinspector.api.protocol.model.ValueWithType
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueRequestData
import com.chimerapps.storageinspector.api.protocol.model.key_value.KeyValueRequestType
import com.google.gsonpackaged.annotations.SerializedName

/**
 * @author Nicola Verbeeck
 */
data class SQLQueryResult(
    val columns: List<String>,
    val rows: List<Map<String, Any?>>,
)

enum class SQLRequestType {
    @SerializedName("query")
    QUERY,
}

data class SQLRequest(
    val type: SQLRequestType,
    val data: SQLRequestData?,
)

data class SQLRequestData(
    val id: String,
    val query: String? = null,
)