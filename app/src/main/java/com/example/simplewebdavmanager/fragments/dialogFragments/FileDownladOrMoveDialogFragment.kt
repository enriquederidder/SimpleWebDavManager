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
 * DialogFragment to Move or Download a file.
 * This dialog is launched when the user long clicks on the selected file in the recycler view.
 *
 * @property file the selected file
 */
class FileDownladOrMoveDialogFragment(private val file: File) : DialogFragment() {

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
                    // TODO
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