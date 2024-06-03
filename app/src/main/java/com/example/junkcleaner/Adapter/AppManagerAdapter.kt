package com.example.junkcleaner.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.junkcleaner.DataClass.appManagerDataClass
import com.example.junkcleaner.R
import com.example.junkcleaner.databinding.ActivityAppManagerBinding
import com.example.junkcleaner.databinding.AppManagerCatLayoutBinding
import com.example.junkcleaner.databinding.ItemAppCategoryLayoutBinding

class AppManagerAdapter(var context: Context, val catArr:ArrayList<appManagerDataClass>, private val onClickListener: (String) -> Unit
):
    RecyclerView.Adapter<AppManagerAdapter.ViewHolder>() {

    private var selectedItemPosition: Int = 0

        inner class ViewHolder(val binding:AppManagerCatLayoutBinding):RecyclerView.ViewHolder(binding.root)
        //{
//            init {
//                // Set click listener for the ConstraintLayout
//                binding.categoryConstraint.setOnClickListener {
//                    val position = adapterPosition
//                    if (position != RecyclerView.NO_POSITION) {
//                        // Invoke the click listener passing the category name
//                        onClickListener(catArr[position].name)
//                    }
//                }
//            }
//        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AppManagerCatLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return catArr.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category=catArr[position]
        holder.binding.categoryName.setText(catArr[position].name)
        // Update background based on selection
        if (position == selectedItemPosition) {
            holder.binding.categoryName.background = ContextCompat.getDrawable(context, R.drawable.cat_app_manageer_sec)
        } else {
            holder.binding.categoryName.background = ContextCompat.getDrawable(context, R.drawable.cat_app_manageer)
        }

        // Set click listener
        holder.binding.categoryConstraint.setOnClickListener {
            selectedItemPosition = holder.adapterPosition
            if (selectedItemPosition != RecyclerView.NO_POSITION) {
                onClickListener(category.name)
                notifyDataSetChanged() // Notify all changes, use more specific change notifications if needed for performance
            }
        }

    }

}