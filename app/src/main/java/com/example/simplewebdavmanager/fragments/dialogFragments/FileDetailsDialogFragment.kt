package com.example.simplewebdavmanager.fragments.dialogFragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.simplewebdavmanager.R
import com.example.simplewebdavmanager.adapaters.FilesAdapter
import com.example.simplewebdavmanager.dataSet.File
import com.example.simplewebdavmanager.fragments.ConnectionDetailsFragment
import kotlin.concurrent.thread

class FileDetailsDialogFragment(private val file: File) : DialogFragment() {
    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialogView =
            requireActivity().layoutInflater.inflate(R.layout.dialog_file_details, null)
        val fileName = dialogView.findViewById<EditText>(R.id.editTextFileName)
        fileName.setText(file.name)
        val parentFragment = parentFragment as? ConnectionDetailsFragment
        var connectionDetailsFragment = parentFragment

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
            .setTitle("File Details")
            .setPositiveButton("Rename") { dialog, _ ->
                val newName = fileName.text.toString()
                if (newName.isNotEmpty()) {
                    thread {
                        try {
                            connectionDetailsFragment = parentFragment
                            connectionDetailsFragment?.renameFileOnServer(file.path, newName)
                        } catch (e: Exception) {
                            Log.e("RenameFile", "Error renaming file", e)
                        }
                    }
                    connectionDetailsFragment?.let {
                        val adapter =
                            it.view?.findViewById<RecyclerView>(R.id.recyclerFiles)?.adapter as? FilesAdapter
                        adapter?.renameFile(file, newName)
                    }
                }
                dialog.dismiss()
            }

            .setNeutralButton("Delete") { dialog, _ ->
                thread {
                    try {
                        connectionDetailsFragment = parentFragment
                        connectionDetailsFragment?.deleteFileFromServer(file.path)

                    } catch (e: Exception) {
                        Log.e("DeleteFile", "Error deleting file", e)
                    }
                }
                connectionDetailsFragment?.let {
                    val adapter =
                        it.view?.findViewById<RecyclerView>(R.id.recyclerFiles)?.adapter as? FilesAdapter
                    adapter?.deleteFile(file)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Exit") { dialog, _ ->
                dialog.dismiss()
            }
        return builder.create()
    }

    companion object {
        fun newInstance(file: File): FileDetailsDialogFragment {
            return FileDetailsDialogFragment(file)
        }
    }

}