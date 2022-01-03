package com.chimerapps.storageinspector.api.protocol.model.file

import com.google.gsonpackaged.annotations.SerializedName

/**
 * @author Nicola Verbeeck
 */
data class FileServerValues(
    val id: String,
    val data: List<FileInfo>,
)

data class FileInfo(
    val path: String,
    val size: Long,
)

data class FileServerByteData(
    val id: String,
    val data: String,
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
    val root: String? = null,
    val data: String? = null,
)
