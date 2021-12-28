package com.chimerapps.storageinspector.api.protocol.model.file

import com.google.gsonpackaged.annotations.SerializedName

/**
 * @author Nicola Verbeeck
 */
data class FileServerValues(
    val id: String,
    val values: List<FileInfo>,
)

data class FileInfo(
    val path: String,
    val size: Long,
)

enum class FileRequestType {
    @SerializedName("list")
    LIST,

    @SerializedName("read")
    READ,

    @SerializedName("write")
    WRITE,

    @SerializedName("remove")
    REMOVE,
}

data class FileRequest(
    val type: FileRequestType,
    val data: FileRequestData?,
)

data class FileRequestData(
    val id: String,
    val path: String? = null,
    val data: String? = null,
)
