package com.example.simplewebdavmanager.fragments.dialogFragments

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
import com.example.simplewebdavmanager.utils.SardineClient

class FileDetailsDialogFragment(
    private val file: File,
    private val sardineClient: SardineClient
) : DialogFragment() {
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
                    sardineClient.renameFile(
                        file.path,
                        newName,
                        connectionDetailsFragment?.currentPath ?: ""
                    ) { success ->
                        if (success) {
                            connectionDetailsFragment?.let {
                                val adapter =
                                    it.view?.findViewById<RecyclerView>(R.id.recyclerFiles)?.adapter as? FilesAdapter
                                adapter?.renameFile(file, newName)
                            }
                        } else {
                            Log.e("RenameFile", "Error renaming file")
                        }
                    }
                }
                dialog.dismiss()
            }
            .setNeutralButton("Delete") { dialog, _ ->
                sardineClient.deleteFile(file.path) { success ->
                    if (success) {
                        connectionDetailsFragment?.let {
                            val adapter =
                                it.view?.findViewById<RecyclerView>(R.id.recyclerFiles)?.adapter as? FilesAdapter
                            adapter?.deleteFile(file)
                        }
                    } else {
                        Log.e("DeleteFile", "Error deleting file")
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Exit") { dialog, _ ->
                dialog.dismiss()
            }
        return builder.create()
    }

    companion object {
        fun newInstance(file: File, sardineClient: SardineClient): FileDetailsDialogFragment {
            return FileDetailsDialogFragment(file, sardineClient)
        }
    }
}
