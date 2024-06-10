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
import com.example.simplewebdavmanager.utils.SardineClient
import com.example.simplewebdavmanager.utils.UIUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine
import java.io.InputStream
import kotlin.concurrent.thread

/**
 * Main fragment for the application, that currently manages the webdav connection and its functionalities,
 * it also manages the recyclers and filepicker
 *
 */
class ConnectionDetailsFragment :
    Fragment(),
    FilesAdapter.OnFileSelectedListener,
    AddressesAdapter.OnAddressSelectedListener {
    private lateinit var v: View

    private lateinit var webDavAddressLiveData: LiveData<String>
    private val possibleWebDavAddressLiveData = MutableLiveData<String>()
    private lateinit var addressesAdapter: AddressesAdapter
    private lateinit var addressesRecyclerView: RecyclerView
    private val discoveredAddresses = mutableListOf<String>()

    private lateinit var btnAddFile: FloatingActionButton
    private lateinit var btnBack: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var filesAdapter: FilesAdapter
    private lateinit var webDavAddress: String
    private lateinit var textSetAddress: TextView
    private lateinit var networkScanner: NetworkScanner
    private lateinit var sardineClient: SardineClient
    var currentPath: String = ""

    // Would like to make this shorter
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
                            sardineClient.uploadFile(fileName, fileContent, currentPath) { success ->
                                if (success) {
                                    activity?.runOnUiThread {
                                        Toast.makeText(
                                            requireContext(),
                                            "File uploaded successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    listAvailableFiles()
                                } else {
                                    activity?.runOnUiThread {
                                        Toast.makeText(
                                            requireContext(),
                                            "Error uploading file",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
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

    /**
     * Called when the fragment is first created.
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = inflater.inflate(R.layout.fragment_connection_details, container, false)

        // Initialize elements in the layout
        btnAddFile = v.findViewById(R.id.floatingActionButtonAddFile)
        btnBack = v.findViewById(R.id.floatingActionButtonBack)
        recyclerView = v.findViewById(R.id.recyclerFiles)
        addressesRecyclerView = v.findViewById(R.id.recyclerAddresses)
        textSetAddress = v.findViewById(R.id.textViewSetAddress)
        btnAddFile.visibility = View.GONE

        // Initialize the recycler for the files
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        filesAdapter = FilesAdapter(mutableListOf(), this)
        recyclerView.adapter = filesAdapter

        // Initialize the recycler for the addresses
        addressesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        addressesAdapter = AddressesAdapter(discoveredAddresses, this)
        addressesRecyclerView.adapter = addressesAdapter

        // Set click listeners for the buttons
        btnAddFile.setOnClickListener {
            openFilePicker(this, pickFile)
        }
        btnBack.setOnClickListener {
            navigateBack()
        }

        // Initialize the webdav address
        val activity = requireActivity() as MainActivity
        webDavAddressLiveData = activity.getWebDavAddressLiveData()
        webDavAddressLiveData.observe(viewLifecycleOwner) { webDavAddress ->
            this.webDavAddress = webDavAddress
            sardineClient = SardineClient(webDavAddress)
            listAvailableFiles()
        }

        // Initiali i want to hide the recycler for the files and show the recycler for the addresses
        addressesRecyclerView.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        return v
    }

    /**
     * Scans the local network for available WebDAV addresses, and updates the UI accordingly.
     *
     */
    fun scanLocalNetwork() {
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

    /**
     * Returns the WebDAV address.
     *
     * @return
     */
    fun getWebDavAddress(): String {
        return webDavAddress
    }

    /**
     * Sets the WebDAV address.
     *
     * @param webDavAddress
     */
    private fun setWebDavAddress(webDavAddress: String) {
        this.webDavAddress = webDavAddress
    }

    /**
     * Called when an address is selected from the addressesRecyclerView, and stops the scanning process.
     *
     * @param address The selected address.
     */
    override fun onAddressSelected(address: String) {
        setWebDavAddress(address)
        sardineClient = SardineClient(address)
        listAvailableFiles()
        addressesRecyclerView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        networkScanner.stopScanning()
    }

    /**
     * Lists the available files on the selected WebDAV server.
     *
     */
    private fun listAvailableFiles(directoryPath: String = "") {
        recyclerView.visibility = View.VISIBLE
        addressesRecyclerView.visibility = View.GONE
        textSetAddress.visibility = View.GONE
        btnAddFile.visibility = View.VISIBLE
        currentPath = directoryPath
        Log.d("WebDavAddress", "Directory Path: $directoryPath")
        sardineClient.listAvailableFiles(directoryPath) { files ->
            activity?.runOnUiThread {
                filesAdapter.updateFiles(files)
                updateBackButtonVisibility()

            }
        }
    }

    /**
     * Called when a file is selected from the filesRecyclerView.
     *
     * @param file
     */
    override fun onFileSelected(file: File) {
        if (file.isDirectory) {
            navigateToDirectory(file)
        } else {
            val dialog = FileDetailsDialogFragment.newInstance(file, sardineClient)
            dialog.show(childFragmentManager, "file_details")
        }
    }

    /**
     * Shows the file details dialog fragment.
     *
     * @param file selected file from the recycler
     */
    override fun onFileSelectedLong(file: File) {
        val dialog = FileDownladOrMoveDialogFragment.newInstance(file)
        dialog.show(childFragmentManager, "file_details")
    }

    /**
     * Navigates back to the parent directory.
     *
     */
    private fun navigateBack() {
        if (currentPath.isNotEmpty()) {
            val parentPath = currentPath.substringBeforeLast("/")
            listAvailableFiles(parentPath)
        }
    }

    /**
     * Navigates to the specified directory on the WebDAV server.
     *
     * @param directory
     */
    private fun navigateToDirectory(directory: File) {
        if (directory.isDirectory) {
            val relativePath = directory.path.removePrefix("http://$webDavAddress/")
            listAvailableFiles(relativePath)
        }
    }

    /**
     * Updates the visibility of the back button based on the current path.
     * If the path is the root directory, the back button is hidden.
     *
     */
    private fun updateBackButtonVisibility() {
        if (currentPath.isEmpty()) {
            UIUtil.hideBackButton(btnBack)
        } else {
            UIUtil.showBackButton(btnBack)
        }
    }

    private fun initSardine(): OkHttpSardine {
        // For the esp that hosts the webdav server its not necessary to set the credentials
        val sardine = OkHttpSardine()
        val userName = ""
        val passWord = ""
        sardine.setCredentials(userName, passWord)
        return sardine
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

    /**
     * Filters the files based on the provided query.
     *
     * @param query
     */
    fun filterFiles(query: String) {
        filesAdapter.filterFiles(query)
    }

    /**
     * Creates a new instance of the fragment and passes args
     */
    companion object {
        @JvmStatic
        fun newInstance() =
            ConnectionDetailsFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}
