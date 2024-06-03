package com.example.junkcleaner.Activity

import android.app.ActivityManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.StatFs
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.junkcleaner.databinding.ActivityDeviceStatusBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale
import java.util.Timer
import java.util.TimerTask


class DeviceStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDeviceStatusBinding
    private val handler = Handler(Looper.getMainLooper())
    private val temperatureUpdateIntervalMs = 5000L

    private val PERMISSION_REQUEST_CODE = 100
    private val PREFS_NAME = "DeviceStatusPrefs"
    private val PERMISSIONS_GRANTED = "permissions_granted"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeviceStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }
        // Fetch device model and OS version
        val deviceModel = Build.MODEL
        val osVersion = Build.VERSION.RELEASE
        //val deviceInfo = "Device Model: $deviceModel\nOS Version: $osVersion"

        // Set the device info to the TextView
        binding.phoneModel.text = deviceModel
        binding.osVersion.text = osVersion

        //CPU Calculate
        startTemperatureUpdates()

        //Storage Calculate
        val totalStorage = getTotalInternalMemorySize()
        val usedStorage = getUsedInternalMemorySize()
        val usedStorageGB = usedStorage.toFloatOrNull() ?: 0f
        val totalStorageGB = totalStorage.toFloatOrNull() ?: 0f
        binding.storageInfo.text = "$usedStorageGB GB / $totalStorageGB GB"
        binding.storageProgressBar.progress=usedStorage.toInt()


        // RAM calculate
        val actManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val totalMemory = memInfo.totalMem
        val availMemory = memInfo.availMem
        val usedMemory = totalMemory - availMemory
        val percent = (usedMemory.toDouble() / totalMemory.toDouble() * 100).toInt()

// Converting bytes into GB
        val totalMemoryGB = totalMemory.toDouble() / (1024 * 1024 * 1024)
        val availMemoryGB = availMemory.toDouble() / (1024 * 1024 * 1024)
        val usedMemoryGB = usedMemory.toDouble() / (1024 * 1024 * 1024)

        Log.d("Memory", "Available: $availMemory, Total: $totalMemory")

// Displaying the memory info into the TextView
        binding.ramInfo.text = String.format("%.2f GB / %.2f GB (RAM used)", usedMemoryGB, totalMemoryGB)
        binding.ramProgressBar.progress = percent

    }


    fun getTotalInternalMemorySize(): String {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        return formatSize(totalBlocks * blockSize)
    }

    fun getUsedInternalMemorySize(): String {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        val usedBlocks = (stat.blockCountLong - availableBlocks) * blockSize
        return formatSize(usedBlocks)
    }

    fun formatSize(size: Long): String {
        var sizeInBytes = size
        var suffix = "B"
        if (sizeInBytes >= 1024) {
            suffix = "KB"
            sizeInBytes /= 1024
            if (sizeInBytes >= 1024) {
                suffix = "MB"
                sizeInBytes /= 1024
                if (sizeInBytes >= 1024) {
                    suffix = "GB"
                    sizeInBytes /=1024
                }
            }
        }
        return sizeInBytes.toString()
    }

    private fun startTemperatureUpdates() {
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val cpuTemperatureString = fetchCPUTemperature()

                val cpuTemperature = cpuTemperatureString.toFloatOrNull() ?: 0f
                // Convert temperature to Fahrenheit
                val temperatureInFahrenheit = (cpuTemperature * 9 / 5000) + 32 // Example conversion

                Log.d("DeviceStatusActivity", "CPU Temperature: $cpuTemperature, Temperature in Fahrenheit: $temperatureInFahrenheit")

                // Update UI with the temperature
                handler.post {
                    binding.cpuInfo.text = String.format(Locale.getDefault(), "%.2f°F", temperatureInFahrenheit)
                    updateCPUProgressBar(temperatureInFahrenheit)
                }
            }
        }, 0, temperatureUpdateIntervalMs)
    }

    private fun updateCPUProgressBar(cpuTemperaturePercentage: Float) {
        Log.d("DeviceStatusActivity", "CPU Temperature Percentage: $cpuTemperaturePercentage")

        // Update the progress of the cpuProgressBar
        binding.cpuProgressBar.progress = cpuTemperaturePercentage.toInt()
    }

    private fun fetchCPUTemperature(): String {
        val command = "cat sys/class/thermal/thermal_zone*/temp"
        val process = Runtime.getRuntime().exec(command)
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var temperature = reader.readLine()?.toFloatOrNull() ?: 0f
        reader.close()
        process.waitFor()
        // Assuming temperature is in millidegrees Celsius
        // You might need to convert it to the appropriate unit based on your requirements
        return temperature.toString()
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null) // Remove all callbacks to avoid memory leaks
    }

}