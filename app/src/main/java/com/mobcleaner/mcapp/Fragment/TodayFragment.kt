package com.mobcleaner.mcapp.Fragment

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobcleaner.mcapp.Adapter.TodayAdapter
import com.mobcleaner.mcapp.DataClass.AppInfo
import com.mobcleaner.mcapp.databinding.FragmentTodayBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class TodayFragment : Fragment() {
    private lateinit var binding: FragmentTodayBinding
    private lateinit var todayAdapter: TodayAdapter
    private val scope = CoroutineScope(Dispatchers.Main)
    private val appUsageTimeMap = mutableMapOf<String, Long>() // Map to store cumulative usage time
    private val INTERVAL_CLEAR_APP_USAGE_MAP = TimeUnit.HOURS.toMillis(1)
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Check if usage access permission is granted
        if (!isUsageAccessGranted()) {
            showPermissionExplanationDialog()
        } else {
            scope.launch {
                initializeFragment()
            }
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("This app needs permissions to access app details like size, installation date, and last used time. Please grant these permissions to proceed.")
            .setPositiveButton("Allow") { _, _ ->
                requestUsageAccessPermission()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private suspend fun initializeFragment() {
        binding.appAnimation.visibility = View.VISIBLE
        // Get the list of apps used today
        val todayAppsList = withContext(Dispatchers.Default) {
            getTodayAppsList()
        }

        // Initialize RecyclerView and Adapter
        todayAdapter = TodayAdapter(todayAppsList)
        binding.TodayRecyclerView.apply {
            adapter = todayAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        binding.appAnimation.visibility = View.GONE
    }

    private fun isUsageAccessGranted(): Boolean {
        val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val packageName = requireContext().packageName
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageAccessPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivity(intent)
    }

    private suspend fun getTodayAppsList(): List<AppInfo> {
        val usageStatsManager =
            requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - TimeUnit.DAYS.toMillis(1)
        val appsList = mutableListOf<AppInfo>()

        val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)
        val event = UsageEvents.Event()
        val packageNameSet = mutableSetOf<String>()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                if (!packageNameSet.contains(event.packageName)) {
                    packageNameSet.add(event.packageName)
                    val appInfo = getAppInfo(event.packageName)
                    appsList.add(appInfo)
                }
            }
        }

        return appsList
    }

    private suspend fun getAppInfo(packageName: String): AppInfo {
        val packageManager = requireContext().packageManager
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val appName = packageManager.getApplicationLabel(applicationInfo).toString()
        val appIcon = packageManager.getApplicationIcon(applicationInfo)
        val lastUsed = getLastUsedTime(packageName)

        return AppInfo(appName, appIcon, lastUsed)
    }

    private suspend fun getLastUsedTime(packageName: String): Long {
        return withContext(Dispatchers.Default) {
            val usageStatsManager =
                requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val beginTime = endTime - TimeUnit.DAYS.toMillis(1)
            var lastTimeUsed = 0L

            val events = usageStatsManager.queryEvents(beginTime, endTime)
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND && event.packageName == packageName) {
                    lastTimeUsed = event.timeStamp
                }
            }

            lastTimeUsed
        }
    }
}

