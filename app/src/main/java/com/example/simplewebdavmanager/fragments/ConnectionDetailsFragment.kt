package com.example.simplewebdavmanager.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simplewebdavmanager.dataSet.File
import com.example.simplewebdavmanager.R
import com.example.simplewebdavmanager.activities.MainActivity
import com.example.simplewebdavmanager.adapaters.FilesAdapter
import com.example.simplewebdavmanager.fragments.dialogFragments.FileDetailsDialogFragment
import com.example.simplewebdavmanager.fragments.dialogFragments.FileDownladOrMoveDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import java.io.FileOutputStream
import java.io.InputStream
import java.util.regex.Pattern
import kotlin.concurrent.thread

class ConnectionDetailsFragment : Fragment(), FilesAdapter.OnFileSelectedListener {
    private lateinit var webDavAddressLiveData: LiveData<String>

    private lateinit var v: View
    private lateinit var btnAddFile: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var filesAdapter: FilesAdapter
    private lateinit var webDavAddress: String
    private lateinit var textSetAddress: TextView

    private val pickFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    val fileName = getFileName(uri)
                    if (fileName != null && isGcodeFile(fileName)) {
                        val inputStream: InputStream? =
                            requireContext().contentResolver.openInputStream(uri)
                        val content = inputStream?.bufferedReader().use { it?.readText() }
                        inputStream?.close()
                        content?.let { fileContent ->
                            thread {
                                val sardine = initSardine()
                                uploadFile(sardine, fileName, fileContent)
                            }
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Please select a file",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_connection_details, container, false)
        btnAddFile = v.findViewById(R.id.floatingActionButtonAddFile)
        recyclerView = v.findViewById(R.id.recyclerFiles)

        textSetAddress = v.findViewById(R.id.textViewSetAddress)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        filesAdapter = FilesAdapter(mutableListOf(), this)
        recyclerView.adapter = filesAdapter

        btnAddFile.setOnClickListener {
            openFilePicker()
        }

        val activity = requireActivity() as MainActivity
        webDavAddressLiveData = activity.getWebDavAddressLiveData()
        webDavAddressLiveData.observe(viewLifecycleOwner) { webDavAddress ->
            this.webDavAddress = webDavAddress
        }

        return v
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Observe changes in the LiveData webDavAddressLiveData
        webDavAddressLiveData.observe(viewLifecycleOwner) { webDavAddress ->
            this.webDavAddress = webDavAddress
            // Call listAvailableFiles() when the WebDAV address is set
            listAvailableFiles()
            textSetAddress.visibility = View.GONE

        }
    }
    fun listAvailableFiles() {
        thread {
            val sardine = initSardine()
            try {
                Log.d("WebDavAddress", "http://$webDavAddress/")
                val files = sardine.list("http://$webDavAddress/")
                val fileList = mutableListOf<File>()
                for (file in files) {
                    val fileName = file.name
                    val filePath = file.href
                    if (file.name.isNotBlank() && file.name != "SETUP.ini"){ // Skip the SETUP.ini file
                        fileList.add(File(fileName, filePath.toString(),  0, ""))
                    }
                }
                activity?.runOnUiThread {
                    filesAdapter.updateFiles(fileList)
                }
            } catch (e: Exception) {
                Log.e("WebDAVFiles", "Error listing files", e)
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        pickFile.launch(intent)
    }

    fun initSardine(): OkHttpSardine {
        // For the esp that hosts the webdav server its no necessary to set the credentials
        val sardine = OkHttpSardine()
        val userName = ""
        val passWord = ""
        sardine.setCredentials(userName, passWord)
        return sardine
    }

    private fun uploadFile(sardine: OkHttpSardine, fileName: String, fileContent: String) {
        val filePath =
            "http://$webDavAddress/$fileName" // Include the original file name in the path
        val data = fileContent.toByteArray()
        sardine.put(filePath, data)
    }

    @SuppressLint("Range")
    private fun getFileName(uri: android.net.Uri): String? {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                return it.getString(it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME))
            }
        }
        return null
    }

    private fun isGcodeFile(fileName: String): Boolean {
        val pattern = Pattern.compile(".*$", Pattern.CASE_INSENSITIVE)
        return pattern.matcher(fileName).matches()
    }

    override fun onFileSelected(file: File) {
        val dialog = FileDetailsDialogFragment.newInstance(file)
        dialog.show(childFragmentManager, "file_details")

    }

    override fun onFileSelectedLong(file: File) {
        val dialog = FileDownladOrMoveDialogFragment.newInstance(file)
        dialog.show(childFragmentManager, "file_details")
    }

    fun deleteFileFromServer(filePath: String) {
        val sardine = initSardine()
        val completeFilePath = "http://$webDavAddress/$filePath"  // Construct the complete URL
        sardine.delete(completeFilePath)
    }

    fun renameFileOnServer(filePath: String, newFileName: String) {
        thread {
            val sardine = initSardine()
            try {
                val completeFilePath = "http://$webDavAddress/$filePath" // Original file path
                val newFilePath = "http://$webDavAddress/${newFileName}" // New file path
                sardine.move(completeFilePath, newFilePath)
            } catch (e: Exception) {
                Log.e("RenameFile", "Error renaming file", e)
            }
        }
    }

    fun downloadFileFromServer(file: File) {
        val sardine = initSardine()
        val completeFilePath = "http://$webDavAddress/${file.path}"   // Assuming file.path contains the full path

        thread {
            try {
                val inputStream = sardine.get(completeFilePath)  // Download file content

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Use MediaStore API for API 29 and above
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, file.name)
                        put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val resolver = requireContext().contentResolver
                    val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

                    uri?.let {
                        resolver.openOutputStream(it).use { outputStream ->
                            inputStream.copyTo(outputStream!!)
                        }
                    }
                } else {

                }

                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "File downloaded successfully to Downloads folder", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("Download", "Error downloading file", e)
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error downloading file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    companion object {
        @JvmStatic
        fun newInstance() =
            ConnectionDetailsFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}
