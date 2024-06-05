package com.example.simplewebdavmanager.adapaters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simplewebdavmanager.R
import com.example.simplewebdavmanager.dataSet.File

/**
 * Adapter for the RecyclerView in the ConnectionDetailFragment, this will show all the files with its type image
 * Also calculates the bytes to the appropriate unit like KB, MB, GB
 *
 * @property files the file retrived from the webdav server
 * @property listener the listener for the file selection
 */
class FilesAdapter(
    private var files: MutableList<File>,
    private val listener: OnFileSelectedListener
) : RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

    private var filteredFiles: MutableList<File> = files // Initialize filteredFiles with files

    /**
     * Create a new ViewHolder
     *
     * @param parent
     * @param viewType
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_files, parent, false)
        return ViewHolder(view)
    }

    /**
     * Bind the data to the ViewHolder
     * Where i also check if a file is a dir or not, ans also set its corresponding image, and also the size
     * @param holder
     * @param position position of the current file
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = filteredFiles[position]
        holder.fileNameTextView.text = file.name
        val fileSize = file.size
        if (fileSize < 0) { // if it is a folder
            holder.fileSizeView.text = ""
        } else {
            holder.fileSizeView.text = when { // Convert the bytes to the appropriate unit
                fileSize < 1024 -> "$fileSize B"
                fileSize < 1024 * 1024 -> "${fileSize / 1024} KB"
                fileSize < 1024 * 1024 * 1024 -> "${fileSize / (1024 * 1024)} MB"
                else -> "${fileSize / (1024 * 1024 * 1024)} GB"
            }
        }

        when (file.type) { // Set the corresponding image based on the file type
            "png", "jpg", "jpeg" -> holder.imageView.setImageResource(R.drawable.image_document)
            "pdf" -> holder.imageView.setImageResource(R.drawable.pdf_document)
            "txt" -> holder.imageView.setImageResource(R.drawable.txt_documen)
            "mp3" -> holder.imageView.setImageResource(R.drawable.audio_document)
            "mp4" -> holder.imageView.setImageResource(R.drawable.video_document)
            "zip" -> holder.imageView.setImageResource(R.drawable.zip_document)
            "xml" -> holder.imageView.setImageResource(R.drawable.xml_document)
            "exe" -> holder.imageView.setImageResource(R.drawable.exe_document)
            "docx", "doc", "docm", "dot", "dotx", "dotm" -> holder.imageView.setImageResource(R.drawable.word_document)
            else -> holder.imageView.setImageResource(android.R.drawable.ic_menu_report_image) // Default image
        }
        // If folder set folder image
        if (file.isDirectory) {
            holder.imageView.setImageResource(R.drawable.folder)
        }
    }

    /**
     * Get the number of files
     *
     * @return
     */
    override fun getItemCount(): Int {
        return filteredFiles.size
    }

    /**
     * Update the files list
     *
     * @param fileList
     */
    fun updateFiles(fileList: List<File>) {
        files = fileList.toMutableList()
        filterFiles("")
    }

    /**
     * Add a new file to the list
     *
     * @param file
     */
    fun deleteFile(file: File) {
        val index = files.indexOf(file)
        if (index != -1) {
            files.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    /**
     * Rename a file
     *
     * @param file
     * @param newName
     */
    fun renameFile(file: File, newName: String) {
        val index = files.indexOf(file)
        if (index != -1) {
            files[index].name = newName
            notifyItemChanged(index)
        }
    }

    /**
     * Filter the files based on the query
     *
     * @param query strign introduced by the user
     */
    fun filterFiles(query: String) {
        filteredFiles = if (query.isEmpty()) {
            files
        } else {
            files.filter { it.name.contains(query, ignoreCase = true) }.toMutableList()
        }
        notifyDataSetChanged()
    }

    /**
     * ViewHolder class for the files
     * Where i set the click listener for the file selection, both for thr file deatials(delete rename)
     * and for long click it will ask for download or move
     * @param itemView
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameTextView: TextView = itemView.findViewById(R.id.textViewFileName)
        val fileSizeView: TextView = itemView.findViewById(R.id.textViewSize)
        val imageView: ImageView = itemView.findViewById(R.id.imageViewFileTypeIcon)

        init {
            itemView.setOnClickListener {// Normal click
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val file = filteredFiles[position]
                    listener.onFileSelected(file)
                }
            }
            itemView.setOnLongClickListener {// Long click
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val file = filteredFiles[position]
                    listener.onFileSelectedLong(file)
                    true
                } else {
                    false
                }
            }
        }
    }

    /**
     * Interface for the file selection
     */
    interface OnFileSelectedListener {
        fun onFileSelected(file: File)
        fun onFileSelectedLong(file: File)
    }

}

