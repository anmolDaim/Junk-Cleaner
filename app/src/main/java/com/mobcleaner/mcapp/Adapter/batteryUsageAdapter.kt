package com.mobcleaner.mcapp.Adapter

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mobcleaner.mcapp.DataClass.batteryUsageDataClass
import com.mobcleaner.mcapp.R
import com.mobcleaner.mcapp.databinding.BatteryUsageLayoutBinding

class batteryUsageAdapter(val usageArr: MutableList<batteryUsageDataClass>):RecyclerView.Adapter<batteryUsageAdapter.ViewHolder>() {

    class ViewHolder(val binding:BatteryUsageLayoutBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = BatteryUsageLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return usageArr.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.binding.batteryUsageImage.setImageDrawable(usageArr[position].icon)
        holder.binding.batteruUsageName.setText(usageArr[position].name)
        holder.binding.batteryUsageTime.setText(usageArr[position].time)
        holder.binding.batteryUsageGrade.setText(usageArr[position].grade)
        // Set progress bar color based on grade
        when (usageArr[position].grade) {
            "Low" -> holder.binding.batteryUsageProgressBar.progressDrawable.setColorFilter(
                ContextCompat.getColor(holder.itemView.context, R.color.green), PorterDuff.Mode.SRC_IN)
            "Medium" -> holder.binding.batteryUsageProgressBar.progressDrawable.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.yellow), PorterDuff.Mode.SRC_IN)
            "High" -> holder.binding.batteryUsageProgressBar.progressDrawable.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.red), PorterDuff.Mode.SRC_IN)
        }

        // Set progress bar progress
        holder.binding.batteryUsageProgressBar.progress = calculateProgress(usageArr[position].time)



    }
    private fun calculateProgress(time: String): Int {
        val timeParts = time.split(", ")
        var timeDays = 0
        var timeHMS = time

        if (timeParts.size > 1) {
            timeDays = timeParts[0].split(" ")[0].toIntOrNull() ?: 0
            timeHMS = timeParts[1]
        }

        val hmsParts = timeHMS.split(":").map { it.toIntOrNull() ?: 0 }
        val hours = hmsParts[0]
        val minutes = hmsParts.getOrNull(1) ?: 0
        val seconds = hmsParts.getOrNull(2) ?: 0

        return (timeDays * 24 * 3600 + hours * 3600 + minutes * 60 + seconds) / (24 * 3600 / 100)
    }


}