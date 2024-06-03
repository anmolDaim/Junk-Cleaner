package com.example.junkcleaner.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.junkcleaner.DataClass.AppInfo
import com.example.junkcleaner.databinding.FragmentThisYearBinding
import com.example.junkcleaner.databinding.ItemGameAppLayoutBinding
import com.example.junkcleaner.databinding.ThisYearLayoutBinding
import java.util.concurrent.TimeUnit

class ThisYearAdapter(private val appUsageList: List<AppInfo>): RecyclerView.Adapter<ThisYearAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ThisYearLayoutBinding):RecyclerView.ViewHolder(binding.root){
        fun bind(appUsageInfo: AppInfo) {
            binding.thisYearAppName.text = appUsageInfo.appName
            binding.thisYearAppImage.setImageDrawable(appUsageInfo.appImage)
            binding.thisYearTimeSpent.text = "${formatTime(appUsageInfo.lastUsed)}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ThisYearLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return appUsageList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(appUsageList[position])
    }
    private fun formatTime(timeInMillis: Long): String {
        // Convert time in milliseconds to hours and minutes
        val hours = TimeUnit.MILLISECONDS.toHours(timeInMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInMillis) % 60
        return String.format("%02d:%02d", hours, minutes)
    }

}