package com.example.simplewebdavmanager.dataSet

import java.util.Date

data class File(
    var name: String,
    val path: String,
    val size: Long,
    val type: String,
    val lastModified: Date,
    val isDirectory: Boolean
)
