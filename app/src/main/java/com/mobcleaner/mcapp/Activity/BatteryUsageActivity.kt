package com.mobcleaner.mcapp.Activity

import android.Manifest
import android.app.usage.StorageStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Bundle
import android.os.storage.StorageManager
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobcleaner.mcapp.Adapter.batteryUsageAdapter
import com.mobcleaner.mcapp.DataClass.batteryUsageDataClass
import com.mobcleaner.mcapp.databinding.ActivityBatteryUsageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class BatteryUsageActivity : AppCompatActivity(),CoroutineScope {

    private lateinit var binding: ActivityBatteryUsageBinding
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private lateinit var packageManager: PackageManager
    private val PERMISSION_REQUEST_CODE = 100
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var storageStatsManager: StorageStatsManager
    private lateinit var storageManager: StorageManager
    private lateinit var appList: MutableList<batteryUsageDataClass>

    // SharedPreferences to store the permissions state
    private val PREFS_NAME = "AppManagerPrefs"
    private val PERMISSIONS_GRANTED = "permissions_granted"

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancel all coroutines when the activity is destroyed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityBatteryUsageBinding.inflate(layoutInflater)
        //enableEdgeToEdge()
        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
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
            appList = mutableListOf()
            fetchAppUsage()
        }
        // Calculate total time

        // Calculate total time
        launch {
           val batteryManager = getSystemService(BATTERY_SERVICE) as BatteryManager

            val batteryUsage = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            val totalSeconds = batteryUsage / 60 // convert to seconds
            val totalMinutes = totalSeconds / 60 // convert to minutes
            val totalHours = totalMinutes / 60 // convert to hours

            val hours = totalHours % 24 // get hours in 24-hour format
            val minutes = (totalMinutes % 3600) / 60 // get minutes
            val seconds = totalSeconds % 60 // get seconds

            // Display total time
            binding.hoursTextView.text = hours.toString()
            binding.minutesTextView.text = minutes.toString()
            binding.secondsTextView.text = seconds.toString()
        }
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

        binding.appAnimation.visibility = View.VISIBLE

        launch(Dispatchers.IO) {
            try {
                val endTime = System.currentTimeMillis()
                val startTime = endTime - TimeUnit.DAYS.toMillis(7) // Last 7 days

                val usageStatsList = withContext(Dispatchers.IO) {
                    usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime)
                }

                if (usageStatsList!= null) {
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

                    withContext(Dispatchers.Main) {
                        binding.batterySaverRecycerView.layoutManager = LinearLayoutManager(this@BatteryUsageActivity)
                        binding.batterySaverRecycerView.adapter = batteryUsageAdapter(appList)
                        binding.appAnimation.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun formatTime(totalTime: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(totalTime)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(totalTime) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime) % 60

        if (hours > 0) {
            return String.format("%d hour, %02d min, %02d sec", hours, minutes, seconds)
        } else if (minutes > 0) {
            return String.format("%d min, %02d sec", minutes, seconds)
        } else {
            return String.format("%d sec", seconds)
        }
    }

    private fun categorizeUsage(totalTime: Long): String {
        val days = TimeUnit.MILLISECONDS.toDays(totalTime)
        return when {
            days < 1 -> "Low"
            days in 1..7 -> "Medium"
            else -> "High"
        }
    }

}