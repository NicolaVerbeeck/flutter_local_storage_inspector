package com.chimerapps.storageinspector.api.protocol.model.sql

import com.google.gsonpackaged.annotations.SerializedName

/**
 * @author Nicola Verbeeck
 */
data class SQLServerIdentification(
    val id: String,
    val name: String,
    val icon: String?,
    val version: Int?,
    val tables: List<SQLTableDefinition>,
)

data class SQLTableDefinition(
    val name: String,
    val primaryKey: List<String>,
    val columns: List<SQLColumnDefinition>,
    val extensions: List<SQLTableExtension>,
)

data class SQLColumnDefinition(
    val name: String,
    val optional: Boolean,
    val type: SQLDataType,
    val nullable: Boolean,
    val autoIncrement: Boolean,
)

enum class SQLTableExtension {
    @SerializedName("withoutRowId")
    WITHOUT_ROW_ID,
}

enum class SQLDataType {
    @SerializedName("text")
    TEXT,

    @SerializedName("blob")
    BLOB,

    @SerializedName("real")
    REAL,

    @SerializedName("integer")
    INTEGER,

    @SerializedName("boolean")
    BOOLEAN,

    @SerializedName("datetime")
    DATETIME,
}