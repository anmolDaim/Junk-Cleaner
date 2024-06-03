package com.example.junkcleaner.Adapter

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.junkcleaner.DataClass.batteryUsageDataClass
import com.example.junkcleaner.R
import com.example.junkcleaner.databinding.BatteryUsageLayoutBinding

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
        val timeParts = time.split(":")
        val hours = timeParts[0].toInt()
        val minutes = timeParts[1].toInt()
        val seconds = timeParts[2].toInt()

        val totalSeconds = hours * 3600 + minutes * 60 + seconds
        val maxSecondsInDay = 24 * 3600 // 24 hours in seconds
        val progress = (totalSeconds.toFloat() / maxSecondsInDay.toFloat()) * 100

        return progress.toInt()
    }


}