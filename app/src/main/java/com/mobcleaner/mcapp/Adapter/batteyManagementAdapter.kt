package com.mobcleaner.mcapp.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.mobcleaner.mcapp.DataClass.batteryManagementDataClass
import com.mobcleaner.mcapp.R

class batteyManagementAdapter(val batteryItems:ArrayList<batteryManagementDataClass>
, private val onClickListener: (String) -> Unit) : RecyclerView.Adapter<batteyManagementAdapter.ViewHolder>() {
   inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val batteryImage: ImageView =itemView.findViewById(R.id.batterImage)
        val batteryName: TextView =itemView.findViewById(R.id.batteryTitle)
       val batteryManagementConstraintLayout:ConstraintLayout=itemView.findViewById(R.id.batteryManagementConstraintLayout)
        init {
            // Set click listener for the ConstraintLayout
            batteryManagementConstraintLayout.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Invoke the click listener passing the category name
                    onClickListener(batteryItems[position].title)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view= LayoutInflater.from(parent.context).inflate(R.layout.battery_management_layout,parent,false)
        return ViewHolder(view)


    }

    override fun getItemCount(): Int {
       return batteryItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.batteryImage.setImageResource(batteryItems[position].image)
        holder.batteryName.setText(batteryItems[position].title)

    }
}