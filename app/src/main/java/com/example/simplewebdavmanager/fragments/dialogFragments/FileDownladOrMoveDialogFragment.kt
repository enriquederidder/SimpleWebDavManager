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

class FileDownladOrMoveDialogFragment(private val file: File) : DialogFragment() {

    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialogView =
            requireActivity().layoutInflater.inflate(R.layout.dialog_download_or_move, null)

        var connectionDetailsFragment = parentFragment as? ConnectionDetailsFragment

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
            .setTitle("File Downlad or Move")
            .setPositiveButton("Download") { dialog, _ ->
                thread {

                    connectionDetailsFragment = parentFragment as? ConnectionDetailsFragment
                    connectionDetailsFragment?.downloadFileFromServer(file)
                }

                dialog.dismiss()
            }

            .setNeutralButton("Move") { dialog, _ ->

                dialog.dismiss()
            }
            .setNegativeButton("Exit") { dialog, _ ->
                dialog.dismiss()
            }
        return builder.create()
    }
    companion object {
        fun newInstance(file: File): FileDownladOrMoveDialogFragment {
            return FileDownladOrMoveDialogFragment(file)
        }
    }
}