package com.mobcleaner.app.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.mobcleaner.app.DataClass.batteryCategoryDataClass
import com.mobcleaner.app.R

class BatteryCategoryAdapter(val catItems:ArrayList<batteryCategoryDataClass>,
                             private val onClickListener: (String) -> Unit) : RecyclerView.Adapter<BatteryCategoryAdapter.ViewHolder>() {

   inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        val catigoryImage:ImageView=itemView.findViewById(R.id.catImage)
        val catigroyName:TextView=itemView.findViewById(R.id.catName)
        val categoryTitle:TextView=itemView.findViewById(R.id.catTitle)
        val batteryCategoryConstraintLayout: ConstraintLayout =itemView.findViewById(R.id.batteryCategoryConstraintLayout)

        init {
            // Set click listener for the ConstraintLayout
            batteryCategoryConstraintLayout.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Invoke the click listener passing the category name
                    onClickListener(catItems[position].name)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
val view=LayoutInflater.from(parent.context).inflate(R.layout.junk_file_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return catItems.size

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.catigoryImage.setImageResource(catItems[position].image)
        holder.categoryTitle.setText(catItems[position].title)
        holder.catigroyName.setText(catItems[position].name)

    }
}