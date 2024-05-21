package com.example.simplewebdavmanager.fragments.dialogFragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.simplewebdavmanager.R
import com.example.simplewebdavmanager.dataSet.File

class FileDetailsDialogFragment(private val file: File) : DialogFragment() {
    @SuppressLint("MissingInflatedId")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialogView =
            requireActivity().layoutInflater.inflate(R.layout.dialog_file_details, null)
        val fileName = dialogView.findViewById<EditText>(R.id.editTextFileName)
        fileName.setText(file.name)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
            .setTitle("File Details")
            .setPositiveButton("Set") { dialog, _ ->



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