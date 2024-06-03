package com.example.junkcleaner.Activity

import android.Manifest
import android.app.usage.StorageStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.storage.StorageManager
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.junkcleaner.Adapter.batteryUsageAdapter
import com.example.junkcleaner.DataClass.batteryUsageDataClass
import com.example.junkcleaner.databinding.ActivityBatteryUsageBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class BatteryUsageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBatteryUsageBinding
    private lateinit var packageManager: PackageManager
    private val PERMISSION_REQUEST_CODE = 100
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var storageStatsManager: StorageStatsManager
    private lateinit var storageManager: StorageManager
    private lateinit var appList: MutableList<batteryUsageDataClass>

    // SharedPreferences to store the permissions state
    private val PREFS_NAME = "AppManagerPrefs"
    private val PERMISSIONS_GRANTED = "permissions_granted"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityBatteryUsageBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        packageManager = applicationContext.packageManager

        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        storageStatsManager = getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        storageManager = getSystemService(Context.STORAGE_SERVICE) as StorageManager

        binding.backBtn.setOnClickListener {
            finish()
        }

        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val permissionsGranted = sharedPreferences.getBoolean(PERMISSIONS_GRANTED, false)

        // Check if permissions are granted, if not, request them
        if (!permissionsGranted) {
            showPermissionExplanationDialog()
        } else {
            // Permissions are already granted, proceed with fetching app details
            fetchAppUsage()
        }
        // Calculate total time
        val totalTime = appList.sumBy {
            val timeParts = it.time.split(":")
            val hours = timeParts[0].toInt()
            val minutes = timeParts[1].toInt()
            val seconds = timeParts[2].toInt()
            hours * 3600 + minutes * 60 + seconds
        }

        // Convert total time to hours, minutes, and seconds
        val totalHours = totalTime / 3600
        val totalMinutes = (totalTime % 3600) / 60
        val totalSeconds = totalTime % 60

        // Display total time
        binding.hoursTextView.text = totalHours.toString()
        binding.minutesTextView.text = totalMinutes.toString()
        binding.secondsTextView.text = totalSeconds.toString()
    }
    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs permissions to access app details like size, installation date, and last used time. Please grant these permissions to proceed.")
            .setPositiveButton("Allow") { _, _ ->
                requestPermissions()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.GET_PACKAGE_SIZE,
                Manifest.permission.PACKAGE_USAGE_STATS,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            PERMISSION_REQUEST_CODE
        )
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                savePermissionsState(true)
                fetchAppUsage()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Permission Denied")
                    .setMessage("Without these permissions, the app cannot function properly. Please grant the permissions from settings.")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }
    }

    private fun savePermissionsState(granted: Boolean) {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(PERMISSIONS_GRANTED, granted)
        editor.apply()
    }

    private fun fetchAppUsage() {
        Log.d("AppManagerActivity", "Fetching installed app usage data...")

        try {
            val endTime = System.currentTimeMillis()
            val startTime = endTime - TimeUnit.DAYS.toMillis(1) // Last 24 hours

            val usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)

            if (usageStatsList != null) {
                appList = mutableListOf()

                val packageManager = packageManager
                usageStatsList.forEach { usageStats ->
                    try {
                        val appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(usageStats.packageName, PackageManager.GET_META_DATA)).toString()
                        val appIcon = packageManager.getApplicationIcon(usageStats.packageName)
                        val totalTime = usageStats.totalTimeInForeground
                        Log.d("AppManagerActivity", "App: $appName, Total Time: $totalTime ms")


                        val totalTimeFormatted = formatTime(totalTime)
                        val usageCategory = categorizeUsage(totalTime)

                        appList.add(
                            batteryUsageDataClass(
                                appName,
                                appIcon,
                                usageStats.packageName,
                                totalTimeFormatted,
                                usageCategory
                            )
                        )

                    } catch (e: PackageManager.NameNotFoundException) {
                        e.printStackTrace()
                    }
                }

                binding.batterySaverRecycerView.layoutManager = LinearLayoutManager(this)
                binding.batterySaverRecycerView.adapter = batteryUsageAdapter(appList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun formatTime(totalTime: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(totalTime)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
    private fun categorizeUsage(totalTime: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(totalTime)
        return when {
            hours < 1 -> "Low"
            hours in 1..3 -> "Medium"
            else -> "High"
        }
    }

}