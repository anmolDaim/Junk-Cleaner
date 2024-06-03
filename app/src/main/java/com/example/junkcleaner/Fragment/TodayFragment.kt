package com.example.junkcleaner.Fragment

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.junkcleaner.DataClass.AppInfo
import com.example.junkcleaner.databinding.FragmentTodayBinding
import java.util.concurrent.TimeUnit


class TodayFragment : Fragment() {
    private lateinit var binding: FragmentTodayBinding
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
            initializeFragment()
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
    private fun initializeFragment() {
        // Get the package name of the current foreground app
        val packageName = getCurrentForegroundAppPackageName()
        // Now you have the package name, you can do further processing
        val appInfo = getAppInfo(packageName)
        // Update UI with app info
        updateUI(appInfo)
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
    private fun getCurrentForegroundAppPackageName(): String {
        val usageStatsManager = requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 1000 * 60 // 1 minute ago
        val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)
        val event = UsageEvents.Event()
        var packageName = ""
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                packageName = event.packageName
            }
        }
        return packageName
    }
    private fun getAppInfo(packageName: String):AppInfo {
        val packageManager = requireContext().packageManager
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        val appName = packageManager.getApplicationLabel(applicationInfo).toString()
        val appIcon = packageManager.getApplicationIcon(applicationInfo)
        val lastUsedTime = getLastUsedTime(packageName)
        Log.d("lastUsedTime","$lastUsedTime,$appName")
        return AppInfo(appName, appIcon, lastUsedTime)
    }
    private fun getLastUsedTime(packageName: String): Long {

        val usageStatsManager = requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - TimeUnit.DAYS.toMillis(1)

        val usageEvents = usageStatsManager.queryEvents(beginTime, endTime)
        val event = UsageEvents.Event()

        var lastTimeUsed = 0L

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND && event.packageName == packageName) {
                // Update the last time used if the current event corresponds to the specified package
                lastTimeUsed = event.timeStamp
            }
        }

        Log.d("getLastUsedTime", "Package: $packageName, Last Used: $lastTimeUsed, Current Time: $endTime")

        return lastTimeUsed
    }


    private fun updateUI(appInfo: AppInfo) {
        binding.todayAppName.text = appInfo.appName
        binding.todayAppImage.setImageDrawable(appInfo.appImage)
        binding.lastUsedApp.text = "${formatLastUsedTime(appInfo.lastUsed)}"

        // Calculate the time passed since the last usage
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - appInfo.lastUsed

        // Calculate the progress percentage
        val progress = (timeDifference.toFloat() / TimeUnit.HOURS.toMillis(24) * 100).toInt()

        // Update the progress bar
        binding.todayProgressBar.progress = progress
    }

    private fun formatLastUsedTime(lastUsedTime: Long): String {
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - lastUsedTime
        Log.d("formatLastUsedTime", "Last Used: $lastUsedTime, Current: $currentTime, Difference: $timeDifference")

        if (timeDifference < 0) {
            Log.w("formatLastUsedTime", "Last used time is in the future. Check system clock.")
            return "Error in time data"
        }

        val hours = TimeUnit.MILLISECONDS.toHours(timeDifference)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference) % 60  // Get remainder after hours
Log.d("formatLastUsedTime","$hours,$minutes")
        return String.format("%d:%d", hours, minutes)
    }
}