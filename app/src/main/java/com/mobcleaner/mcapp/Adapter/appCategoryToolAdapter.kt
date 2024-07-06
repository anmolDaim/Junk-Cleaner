package com.mobcleaner.mcapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobcleaner.mcapp.DataClass.appCategoryToolDataClass
import com.mobcleaner.mcapp.databinding.ItemAppCategoryLayoutBinding

class appCategoryToolAdapter(val arrcat:ArrayList<appCategoryToolDataClass>,
                             private val onClickListener: (String) -> Unit):RecyclerView.Adapter<appCategoryToolAdapter.ViewHolder>() {

    inner class ViewHolder(val binding:ItemAppCategoryLayoutBinding):RecyclerView.ViewHolder(binding.root){
        init {
            // Set click listener for the ConstraintLayout
            binding.itemAppConstraint.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Invoke the click listener passing the category name
                    onClickListener(arrcat[position].name)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppCategoryLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return arrcat.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.itemName.setText(arrcat[position].name)
        holder.binding.itemImage.setImageResource(arrcat[position].image)

    }


}