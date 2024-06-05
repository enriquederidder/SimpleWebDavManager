package com.example.simplewebdavmanager.utils

import com.example.simplewebdavmanager.dataSet.File
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import java.io.IOException
import java.io.InputStream
import kotlin.concurrent.thread

/**
 * Manages the connection to the webdav server via sardine
 *
 * @property webDavAddress The address of the webdav server
 */
class SardineClient(private val webDavAddress: String) {

    private val sardine: OkHttpSardine = OkHttpSardine()

    init {
        sardine.setCredentials("", "")
    }

    fun uploadFile(fileName: String, fileContent: String, callback: (Boolean) -> Unit) {
        thread {
            try {
                val filePath = "http://$webDavAddress/$fileName"
                val data = fileContent.toByteArray()
                sardine.put(filePath, data)
                callback(true)
            } catch (e: IOException) {
                e.printStackTrace()
                callback(false)
            }
        }
    }

    fun deleteFile(filePath: String, callback: (Boolean) -> Unit) {
        thread {
            try {
                val completeFilePath = "http://$webDavAddress/$filePath"
                sardine.delete(completeFilePath)
                callback(true)
            } catch (e: IOException) {
                e.printStackTrace()
                callback(false)
            }
        }
    }

    fun renameFile(
        filePath: String,
        newFileName: String,
        currentPath: String,
        callback: (Boolean) -> Unit
    ) {
        thread {
            try {
                val completeFilePath = "http://$webDavAddress/$filePath"
                val newFilePath = "http://$webDavAddress/$currentPath/$newFileName"
                sardine.move(completeFilePath, newFilePath)
                callback(true)
            } catch (e: IOException) {
                e.printStackTrace()
                callback(false)
            }
        }
    }

    fun downloadFile(file: File, callback: (InputStream?) -> Unit) {
        thread {
            try {
                val completeFilePath = "http://$webDavAddress/${file.path}"
                val inputStream = sardine.get(completeFilePath)
                callback(inputStream)
            } catch (e: IOException) {
                e.printStackTrace()
                callback(null)
            }
        }
    }

    fun listAvailableFiles(directoryPath: String, callback: (List<File>) -> Unit) {
        thread {
            try {
                val fullPath = "http://$webDavAddress$directoryPath"
                val files = sardine.list(fullPath)
                val fileList = files.map { file ->
                    File(
                        name = file.name,
                        path = file.href.toString(),
                        size = file.contentLength,
                        type = file.contentType,
                        lastModified = file.modified,
                        isDirectory = file.isDirectory
                    )
                }.filter { file ->
                    file.name.isNotBlank() && file.name != "SETUP.ini" && file.name != directoryPath.substringAfterLast(
                        "/"
                    )
                }
                callback(fileList)
            } catch (e: IOException) {
                e.printStackTrace()
                callback(emptyList())
            }
        }
    }
}
