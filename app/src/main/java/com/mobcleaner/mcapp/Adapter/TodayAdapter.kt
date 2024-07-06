package com.mobcleaner.mcapp.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobcleaner.mcapp.DataClass.AppInfo
import com.mobcleaner.mcapp.databinding.TodayUsedLayoutBinding
import java.util.concurrent.TimeUnit

class TodayAdapter(private val todayArr: List<AppInfo>) :
    RecyclerView.Adapter<TodayAdapter.Viewholder>() {

    class Viewholder(val binding: TodayUsedLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Viewholder {
        val binding =
            TodayUsedLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Viewholder(binding)
    }

    override fun getItemCount(): Int {
        return todayArr.size
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val todayData = todayArr[position]

        holder.binding.todayAppName.text = todayData.appName
        holder.binding.todayAppImage.setImageDrawable(todayData.appImage)
        holder.binding.lastUsedApp.text = formatLastUsedTime(todayData.lastUsed)

        // Calculate the time passed since the last usage
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - todayData.lastUsed

        // Calculate the progress percentage
        val progress = (timeDifference.toFloat() / TimeUnit.HOURS.toMillis(24) * 100).toInt()

        // Update the progress bar
        holder.binding.todayProgressBar.progress = progress
    }

    private fun formatLastUsedTime(lastUsedTime: Long): String {
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - lastUsedTime

        if (timeDifference < 0) {
            return "Error in time data"
        }

        // Convert timeDifference from milliseconds to seconds
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifference) % 60
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference) % 60
        val hours = TimeUnit.MILLISECONDS.toHours(timeDifference)

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
