package com.example.simplewebdavmanager.fragments.dialogFragments

import android.app.Dialog
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.simplewebdavmanager.R
import com.example.simplewebdavmanager.activities.MainActivity
import com.example.simplewebdavmanager.dataSet.File
import com.example.simplewebdavmanager.utils.SardineClient

class FileDownladOrMoveDialogFragment(
    private val file: File,
    private val sardineClient: SardineClient
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView =
            requireActivity().layoutInflater.inflate(R.layout.dialog_download_or_move, null)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
            .setTitle("File Download or Move")
            .setPositiveButton("Download") { dialog, _ ->
                sardineClient.downloadFile(file) { inputStream ->
                    if (inputStream != null) {
                        val activity = requireActivity() as MainActivity
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val values = ContentValues().apply {
                                put(MediaStore.Downloads.DISPLAY_NAME, file.name)
                                put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                                put(
                                    MediaStore.Downloads.RELATIVE_PATH,
                                    Environment.DIRECTORY_DOWNLOADS
                                )
                            }
                            val resolver = activity.contentResolver
                            val uri: Uri? =
                                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                            uri?.let {
                                resolver.openOutputStream(it).use { outputStream ->
                                    inputStream.copyTo(outputStream!!)
                                }
                            }
                        } else {
                            activity.runOnUiThread {
                                Toast.makeText(
                                    requireContext(),
                                    "Download not supported on this device version",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        activity.runOnUiThread {
                            Toast.makeText(
                                requireContext(),
                                "File downloaded successfully to Downloads folder",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Log.e("DownloadFile", "Error downloading file")
                    }
                }
                dialog.dismiss()
            }
            .setNeutralButton("Move") { dialog, _ ->
                // TODO: Implement move functionality
                dialog.dismiss()
            }
            .setNegativeButton("Exit") { dialog, _ ->
                dialog.dismiss()
            }
        return builder.create()
    }

    companion object {
        fun newInstance(file: File, sardineClient: SardineClient): FileDownladOrMoveDialogFragment {
            return FileDownladOrMoveDialogFragment(file, sardineClient)
        }
    }
}
