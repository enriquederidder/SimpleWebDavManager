package com.example.simplewebdavmanager.utils

import com.example.simplewebdavmanager.dataSet.File
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import java.io.IOException
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

    /**
     * Uploads a file to the webdav server
     *
     * @param fileName The name of the file
     * @param fileContent The content of the file
     * @param callback The callback function
     */
    fun uploadFile(fileName: String, fileContent: String, currentPath: String, callback: (Boolean) -> Unit) {
        thread {
            try {
                val filePath = "http://$webDavAddress/$currentPath/$fileName"
                val data = fileContent.toByteArray()
                sardine.put(filePath, data)
                callback(true)
            } catch (e: IOException) {
                e.printStackTrace()
                callback(false)
            }
        }
    }

    /**
     * Deletes a file from the webdav server
     *
     * @param filePath The path of the file
     * @param callback The callback function
     */
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

    /**
     * Renames a file on the webdav server
     *
     * @param filePath The path of the file
     * @param newFileName The new name of the file
     */
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

    /**
     * Lists the files in a directory on the webdav server
     *
     * @param directoryPath The path of the directory
     * @param callback The callback function
     */
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
                        type = file.name.substringAfterLast("."),
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
