package com.example.simplewebdavmanager.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment

object FilePickerUtil {
    fun openFilePicker(fragment: Fragment, pickFile: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        pickFile.launch(intent)
    }

    @SuppressLint("Range")
    fun getFileName(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME))
            }
        }
        return null
    }
}
