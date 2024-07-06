package com.mobcleaner.mcapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobcleaner.mcapp.DataClass.itemAppManagerDataClass
import com.mobcleaner.mcapp.databinding.AppManagerItemLayoutBinding

class itemAppManagerAdapter(
    private var arrItem: List<itemAppManagerDataClass>,
    private val listener: OnCheckboxChangeListener
    ,private val showDetailsClickListner: DetailsClickListner
) : RecyclerView.Adapter<itemAppManagerAdapter.ViewHolder>() {

    interface DetailsClickListner {
        fun showDetailsClickListner(item: itemAppManagerDataClass)
    }

    interface OnCheckboxChangeListener {
        fun onCheckboxChanged()
    }

    inner class ViewHolder(val binding: AppManagerItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    arrItem[position].isSelected = isChecked
                    listener.onCheckboxChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AppManagerItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrItem.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = arrItem[position]
        holder.binding.AppName.text = item.appName
        holder.binding.AppImage.setImageDrawable(item.appImage)
        holder.binding.checkBox.visibility = item.checkboxVisibility ?: View.VISIBLE

        holder.binding.installed.text = item.appInstall
        holder.binding.size.text = item.appSize

        holder.binding.size.visibility = item.sizeCheckboxVisibility ?: View.VISIBLE
        holder.binding.installed.visibility = item.installCheckboxVisibility ?: View.VISIBLE

        holder.binding.appManagerConstraintLayout.setOnClickListener {
            showDetailsClickListner.showDetailsClickListner(item)
        }
    }
}
