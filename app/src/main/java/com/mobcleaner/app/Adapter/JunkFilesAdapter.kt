package com.mobcleaner.app.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobcleaner.app.DataClass.junkFiles
import com.mobcleaner.app.databinding.ItemJunkFileBindingBinding

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
