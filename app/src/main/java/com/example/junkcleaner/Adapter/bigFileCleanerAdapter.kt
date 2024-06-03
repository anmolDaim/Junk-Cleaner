package com.example.junkcleaner.Adapter

import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.junkcleaner.DataClass.bigFileCleanerDataClass
import com.example.junkcleaner.databinding.BigFileCleanerLayoutBinding
import com.example.junkcleaner.databinding.ItemGameAppLayoutBinding
import java.io.File

class bigFileCleanerAdapter(val bigFile:ArrayList<bigFileCleanerDataClass>
                            ,private val listener: OnCheckboxChangeListener
    , private val itemClickListener: OnItemClickListener
):RecyclerView.Adapter<bigFileCleanerAdapter.ViewHolder>() {


    interface OnCheckboxChangeListener {
        fun onCheckboxChanged()
    }

    interface OnItemClickListener {
        fun onItemClick(item: bigFileCleanerDataClass)
    }

    class ViewHolder(val binding: BigFileCleanerLayoutBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BigFileCleanerLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return bigFile.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = bigFile[position]
        holder.binding.fileName.text = app.fileName
        holder.binding.fileMb.text=app.fileSize
        holder.binding.checkBox.isChecked = app.isSelected
        holder.binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            app.isSelected = isChecked
            listener.onCheckboxChanged()
        }
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(app)
        }

    }
}