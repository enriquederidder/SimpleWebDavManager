package com.example.simplewebdavmanager.fragments.dialogFragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.simplewebdavmanager.R
import com.example.simplewebdavmanager.activities.MainActivity

/**
 * DialogFragment to set the WebDAV address manually
 *
 */
class SetWebDavAddresDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val dialogView =
            requireActivity().layoutInflater.inflate(R.layout.dialog_set_webdav_address, null)
        val webdavAddressText = dialogView.findViewById<EditText>(R.id.editTextWebDavAddress)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
            .setTitle("Set WebDAV Address")
            .setPositiveButton("Set") { dialog, _ ->
                val webdavAddress = webdavAddressText.text.toString()
                Log.d("WebDavAddress", webdavAddress)
                val activity = requireActivity() as MainActivity
                activity.setWebDavAddress(webdavAddress)
                dialog.dismiss()
            }
            .setNegativeButton("Exit") { dialog, _ ->
                dialog.dismiss()
            }

        return builder.create()
    }
}