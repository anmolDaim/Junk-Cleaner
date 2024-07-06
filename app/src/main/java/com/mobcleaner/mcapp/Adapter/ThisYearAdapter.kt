package com.mobcleaner.mcapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobcleaner.mcapp.DataClass.AppInfo
import com.mobcleaner.mcapp.databinding.ThisYearLayoutBinding

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
        val seconds = timeInMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        val remainingSeconds = seconds % 60

        return String.format("%02d:%02d:%02d", hours, remainingMinutes, remainingSeconds)
    }

}