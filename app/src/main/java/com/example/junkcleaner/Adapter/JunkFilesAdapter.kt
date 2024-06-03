package com.example.junkcleaner.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.junkcleaner.DataClass.junkFiles
import com.example.junkcleaner.databinding.ItemJunkFileBindingBinding
import java.io.File

class JunkFilesAdapter(private val junkFiles: ArrayList<junkFiles>) :
    RecyclerView.Adapter<JunkFilesAdapter.JunkFileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JunkFileViewHolder {
        val binding = ItemJunkFileBindingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JunkFileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JunkFileViewHolder, position: Int) {
        val junkFile = junkFiles[position]
        holder.binding.fileName.text=junkFile.junkFileName
    }

    override fun getItemCount(): Int = junkFiles.size

    inner class JunkFileViewHolder(val binding: ItemJunkFileBindingBinding) :RecyclerView.ViewHolder(binding.root)
}
