package com.example.simplewebdavmanager.adapaters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simplewebdavmanager.File
import com.example.simplewebdavmanager.R

class FilesAdapter(private var files: MutableList<File>) :
    RecyclerView.Adapter<FilesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_files, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]
        holder.fileNameTextView.text = file.name
        holder.filePathTextView.text = file.path
    }

    override fun getItemCount(): Int {
        return files.size
    }

    fun updateFiles(fileList: MutableList<File>) {
        files.clear()
        files.addAll(fileList)
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileNameTextView: TextView = itemView.findViewById(R.id.textViewFileName)
        val filePathTextView: TextView = itemView.findViewById(R.id.textViewStatsFile)
    }
}
