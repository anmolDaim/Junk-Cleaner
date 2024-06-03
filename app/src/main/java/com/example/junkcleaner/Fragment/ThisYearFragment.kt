package com.example.junkcleaner.Fragment

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.junkcleaner.Adapter.ThisYearAdapter
import com.example.junkcleaner.DataClass.AppInfo
import com.example.junkcleaner.databinding.FragmentThisYearBinding
import java.util.Calendar


class ThisYearFragment : Fragment() {
    private lateinit var binding: FragmentThisYearBinding
    private lateinit var adapter: ThisYearAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentThisYearBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val appUsageList = getAppUsageList()
        adapter = ThisYearAdapter(appUsageList)
        binding.thisYearRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.thisYearRecyclerView.adapter = adapter
    }
    private fun getAppUsageList(): List<AppInfo> {
        val packageManager = requireActivity().packageManager
        val packageNames = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val startTime = getYearStartTime(currentYear)
        val endTime = System.currentTimeMillis()

        val usageStatsManager =
            requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_YEARLY, startTime, endTime
        )

        val appUsageList = mutableListOf<AppInfo>()
        usageStats.forEach { usageStat ->
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
        }
        return appUsageList
    }

    private fun getYearStartTime(year: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(year, Calendar.JANUARY, 1, 0, 0, 0)
        return calendar.timeInMillis
    }
}