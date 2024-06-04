package com.example.simplewebdavmanager.fragments

import android.app.Activity
import android.content.ContentValues
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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simplewebdavmanager.R
import com.example.simplewebdavmanager.activities.MainActivity
import com.example.simplewebdavmanager.adapaters.AddressesAdapter
import com.example.simplewebdavmanager.adapaters.FilesAdapter
import com.example.simplewebdavmanager.dataSet.File
import com.example.simplewebdavmanager.fragments.dialogFragments.FileDetailsDialogFragment
import com.example.simplewebdavmanager.fragments.dialogFragments.FileDownladOrMoveDialogFragment
import com.example.simplewebdavmanager.utils.FilePickerUtil
import com.example.simplewebdavmanager.utils.FilePickerUtil.openFilePicker
import com.example.simplewebdavmanager.utils.NetworkScanner
import com.example.simplewebdavmanager.utils.UIUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import java.io.IOException
import java.io.InputStream
import kotlin.concurrent.thread

class ConnectionDetailsFragment :
    Fragment(),
    FilesAdapter.OnFileSelectedListener,
    AddressesAdapter.OnAddressSelectedListener
{

    private lateinit var webDavAddressLiveData: LiveData<String>
    private val possibleWebDavAddressLiveData = MutableLiveData<String>()
    private lateinit var addressesAdapter: AddressesAdapter
    private lateinit var addressesRecyclerView: RecyclerView
    private val discoveredAddresses = mutableListOf<String>()

    private lateinit var v: View
    private lateinit var btnAddFile: FloatingActionButton
    private lateinit var btnBack: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var filesAdapter: FilesAdapter
    private lateinit var webDavAddress: String
    private lateinit var textSetAddress: TextView
    private lateinit var networkScanner: NetworkScanner
    private var currentPath: String = ""

    private val pickFile =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let { uri ->
                    val fileName = FilePickerUtil.getFileName(requireContext(), uri)
                    if (fileName != null) {
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
        v = inflater.inflate(R.layout.fragment_connection_details, container, false)
        btnAddFile = v.findViewById(R.id.floatingActionButtonAddFile)
        btnBack = v.findViewById(R.id.floatingActionButtonBack)
        recyclerView = v.findViewById(R.id.recyclerFiles)
        addressesRecyclerView = v.findViewById(R.id.recyclerAddresses)
        textSetAddress = v.findViewById(R.id.textViewSetAddress)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        filesAdapter = FilesAdapter(mutableListOf(), this)
        recyclerView.adapter = filesAdapter

        addressesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        addressesAdapter = AddressesAdapter(discoveredAddresses, this)
        addressesRecyclerView.adapter = addressesAdapter

        btnAddFile.setOnClickListener {
            openFilePicker(this, pickFile)
        }
        btnBack.setOnClickListener {
            navigateBack()
        }
        btnAddFile.visibility = View.GONE

        val activity = requireActivity() as MainActivity
        webDavAddressLiveData = activity.getWebDavAddressLiveData()
        webDavAddressLiveData.observe(viewLifecycleOwner) { webDavAddress ->
            this.webDavAddress = webDavAddress
            // Call listAvailableFiles() when the WebDAV address is set
            listAvailableFiles()
            textSetAddress.visibility = View.GONE
            btnAddFile.visibility = View.VISIBLE
        }

        // Initiali i want to hide the recycler for the files and show the recycler for the addresses
        addressesRecyclerView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        networkScanner = NetworkScanner(requireContext(), possibleWebDavAddressLiveData)
        networkScanner.scanLocalNetwork()

        possibleWebDavAddressLiveData.observe(viewLifecycleOwner, Observer { address ->
            if (address.isNotEmpty() && !discoveredAddresses.contains(address)) {
                discoveredAddresses.add(address)
                addressesAdapter.updateAddresses(discoveredAddresses)
                Log.d("WebDavAddress", "WebDAV Address: $address")
            }
        })
    }

    fun getWebDavAddress(): String {
        return webDavAddress
    }

    fun setWebDavAddress(webDavAddress: String) {
        this.webDavAddress = webDavAddress
    }
    override fun onAddressSelected(address: String) {
        setWebDavAddress(address)
        listAvailableFiles()
        addressesRecyclerView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        networkScanner.stopScanning()
    }


    private fun initSardine(): OkHttpSardine {
        // For the esp that hosts the webdav server its not necessary to set the credentials
        val sardine = OkHttpSardine()
        val userName = ""
        val passWord = ""
        sardine.setCredentials(userName, passWord)
        return sardine
    }

    private fun listAvailableFiles(directoryPath: String = "") {
        recyclerView.visibility = View.VISIBLE
        addressesRecyclerView.visibility = View.GONE
        currentPath = directoryPath
        updateBackButtonVisibility()  // Update visibility of btnBack
        Log.d("WebDavAddress", "Directory Path: $directoryPath")
        thread {
            val sardine = initSardine()
            val fullPath = "http://$webDavAddress$directoryPath"
            Log.d("WebDavAddress", fullPath)
            val maxRetries = 3
            var attempts = 0

            while (attempts < maxRetries) {
                try {
                    val files = sardine.list(fullPath)
                    val fileList = mutableListOf<File>()
                    for (file in files) {
                        val fileName = file.name // File name
                        val filePath = file.href // Absolute path
                        val isDir = file.isDirectory // Check if it's a directory
                        val modifiedDate = file.modified // Last modified date
                        val size = file.contentLength // File size in bytes
                        if (fileName.isNotBlank() && fileName != "SETUP.ini" && fileName != currentPath.substringAfterLast(
                                "/"
                            )
                        ) { // Skip the SETUP.ini file and skip the current directory
                            fileList.add(
                                File(
                                    fileName,
                                    filePath.toString(),
                                    size,
                                    fileName.substringAfterLast("."),
                                    modifiedDate,
                                    isDir
                                )
                            )
                            Log.d(
                                "WebDAVFiles",
                                "File: $fileName " +
                                        "(${if (isDir) "Directory" else "File"}) " +
                                        "- Size: $size bytes " +
                                        "- Modified: $modifiedDate " +
                                        "- Path: $filePath "
                            )
                        }
                    }
                    activity?.runOnUiThread {
                        filesAdapter.updateFiles(fileList)
                    }
                    break // Exit loop if successful
                } catch (e: IOException) {
                    attempts++
                    if (attempts >= maxRetries) {
                        Log.e("WebDAVFiles", "Error listing files after $maxRetries attempts", e)
                    } else {
                        Log.w("WebDAVFiles", "Retrying listing files (attempt $attempts)", e)
                    }
                } catch (e: Exception) {
                    Log.e("WebDAVFiles", "Unexpected error", e)
                    break // Exit loop on non-IOException
                }
            }
        }
    }

    override fun onFileSelected(file: File) {
        if (file.isDirectory) {
            navigateToDirectory(file)
        } else {
            val dialog = FileDetailsDialogFragment.newInstance(file)
            dialog.show(childFragmentManager, "file_details")
        }
    }


    private fun uploadFile(sardine: OkHttpSardine, fileName: String, fileContent: String) {
        val filePath =
            "http://$webDavAddress/$fileName" // Include the original file name in the path
        val data = fileContent.toByteArray()
        sardine.put(filePath, data)
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
                val newFilePath = "http://$webDavAddress/$currentPath/$newFileName" // New file path
                Log.d(
                    "RenameFile",
                    "Original File Path: $completeFilePath New File Path: $newFilePath"
                )
                sardine.move(completeFilePath, newFilePath)
            } catch (e: Exception) {
                Log.e("RenameFile", "Error renaming file", e)
            }
        }
    }

    fun downloadFileFromServer(file: File) {
        val sardine = initSardine()
        val completeFilePath = "http://$webDavAddress/${file.path}"

        thread {
            try {
                val inputStream = sardine.get(completeFilePath) // Download file content

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // MediaStore API
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, file.name)
                        put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                        put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val resolver = requireContext().contentResolver
                    val uri: Uri? =
                        resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

                    uri?.let {
                        resolver.openOutputStream(it).use { outputStream ->
                            inputStream.copyTo(outputStream!!)
                        }
                    }
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            "Download not supported on this device version",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }

                activity?.runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "File downloaded successfully to Downloads folder",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("Download", "Error downloading file", e)
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Error downloading file", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun navigateBack() {
        // Check if the current path is not the root directory
        if (currentPath.isNotEmpty()) {
            // Extract the parent directory path
            val parentPath = currentPath.substringBeforeLast("/")
            // Navigate to the parent directory
            listAvailableFiles(parentPath)
        }
    }

    private fun navigateToDirectory(directory: File) {
        if (directory.isDirectory) {
            val relativePath = directory.path.removePrefix("http://$webDavAddress/")
            listAvailableFiles(relativePath)
        }
    }

    private fun updateBackButtonVisibility() {
        if (currentPath.isEmpty()) {
            UIUtil.hideBackButton(btnBack)
        } else {
            UIUtil.showBackButton(btnBack)
        }
    }

    override fun onFileSelectedLong(file: File) {
        val dialog = FileDownladOrMoveDialogFragment.newInstance(file)
        dialog.show(childFragmentManager, "file_details")
    }

    fun filterFiles(query: String) {
        filesAdapter.filterFiles(query)
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

