package com.example.simplewebdavmanager.fragments.dialogFragments

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.simplewebdavmanager.R
import com.example.simplewebdavmanager.dataSet.File
import com.example.simplewebdavmanager.fragments.ConnectionDetailsFragment
import kotlin.concurrent.thread

/**
 * DialogFragment to show options to download or move a file
 */
class FileDownladOrMoveDialogFragment(private val file: File) : DialogFragment() {

    /**
     * Creates the dialog
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialogView =
            requireActivity().layoutInflater.inflate(R.layout.dialog_download_or_move, null)

        var connectionDetailsFragment: ConnectionDetailsFragment?

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