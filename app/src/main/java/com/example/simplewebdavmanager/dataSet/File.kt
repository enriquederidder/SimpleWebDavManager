package com.example.simplewebdavmanager.dataSet

import java.util.Date

/**
 * Represents a file in the file system.
 *
 * @property name The name of the file.
 * @property path The path to the file.
 * @property size The size of the file in bytes.
 * @property type The type of the file.
 * @property lastModified The last modified date of the file.
 * @property isDirectory Whether the file is a directory.
 */
data class File(
    var name: String,
    val path: String,
    val size: Long,
    val type: String,
    val lastModified: Date,
    val isDirectory: Boolean
)
