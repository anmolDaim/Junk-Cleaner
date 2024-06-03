package com.example.junkcleaner.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.junkcleaner.DataClass.gameAppDataClass
import com.example.junkcleaner.databinding.ItemGameAppLayoutBinding

class gameAppAdapter(val gameArr: MutableList<gameAppDataClass>, private val onItemClick: (gameAppDataClass) -> Unit):RecyclerView.Adapter<gameAppAdapter.ViewHolder>() {

    class ViewHolder(val binding:ItemGameAppLayoutBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGameAppLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return gameArr.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val app = gameArr[position]
        holder.binding.appName.text = app.appName
        holder.binding.appIcon.setImageDrawable(app.appIcon)
// Setup click listener to call onItemClick lambda
        holder.binding.launchBtn.setOnClickListener {
            onItemClick(app)
        }

    }


}