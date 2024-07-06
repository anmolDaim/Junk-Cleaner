package com.mobcleaner.mcapp.Fragment

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobcleaner.mcapp.Adapter.ThisYearAdapter
import com.mobcleaner.mcapp.DataClass.AppInfo
import com.mobcleaner.mcapp.databinding.FragmentThisYearBinding
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext

class ThisYearFragment : Fragment(), CoroutineScope {
    private lateinit var binding: FragmentThisYearBinding
    private lateinit var adapter: ThisYearAdapter
    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancel all coroutines when the fragment is destroyed
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentThisYearBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        launch {
            setupRecyclerView()
        }
    }

    private suspend fun setupRecyclerView() {
        binding.appAnimation.visibility = View.VISIBLE
        try {
            val appUsageList = getAppUsageList()
            // Convert to MutableList and sort in descending order based on totalTime
            val sortedList = appUsageList.toMutableList()
            //sortedList.sortByDescending { it.lastUsed }
            adapter = ThisYearAdapter(sortedList)
            binding.thisYearRecyclerView.layoutManager = LinearLayoutManager(context)
            binding.thisYearRecyclerView.adapter = adapter
        } catch (e: Exception) {
            // Handle any exceptions
            e.printStackTrace()
        } finally {
            binding.appAnimation.visibility = View.GONE
        }
    }

    private suspend fun getAppUsageList(): List<AppInfo> = withContext(Dispatchers.IO) {
        val packageManager = requireActivity().packageManager
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val startTime = getYearStartTime(currentYear)
        val endTime = System.currentTimeMillis()

        val usageStatsManager =
            requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        // Querying usage stats for the interval
        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_YEARLY, startTime, endTime
        )

        // Process usage stats into AppInfo list
        val appUsageList = mutableListOf<AppInfo>()
        usageStats.forEach { usageStat ->
            try {
                val appName = packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(usageStat.packageName, 0)
                ).toString()
                val appIcon = packageManager.getApplicationIcon(usageStat.packageName)
                appUsageList.add(
                    AppInfo(
                        appName,
                        appIcon,
                        usageStat.totalTimeInForeground
                    )
                )
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
            }
        }
        appUsageList
    }

    private fun getYearStartTime(year: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0)
        return calendar.timeInMillis
    }
}
