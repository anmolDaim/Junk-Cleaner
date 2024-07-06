package com.mobcleaner.mcapp.Fragment

import android.app.ActivityManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.StatFs
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity.ACTIVITY_SERVICE
import com.mobcleaner.mcapp.Activity.ScreenCheckActivity
import com.mobcleaner.mcapp.R
import com.mobcleaner.mcapp.databinding.FragmentDeviceStatusBinding
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.DecimalFormat
import java.util.Locale
import java.util.Timer
import java.util.TimerTask

class DeviceStatusFragment : Fragment() {
    private lateinit var binding: FragmentDeviceStatusBinding
    private val handler = Handler(Looper.getMainLooper())
    private val temperatureUpdateIntervalMs = 5000L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDeviceStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.storageCleaner.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(
                R.id.DrawarContainer,
                ColdShowerProcessManagFragment()
            ).commit()
        }
        binding.storageCleaner2.setOnClickListener {
            parentFragmentManager.beginTransaction().replace(
                R.id.DrawarContainer,
                ColdShowerProcessManagFragment()
            ).commit()
        }
        binding.checkScreenBtn.setOnClickListener(){
            val intent= Intent(requireContext(),ScreenCheckActivity::class.java)
            startActivity(intent)
        }

        // Fetch device model and OS version
        val deviceModel = Build.MODEL
        val osVersion = Build.VERSION.RELEASE

        // Set the device info to the TextView
        binding.phoneModel.text = deviceModel
        binding.osVersion.text = osVersion

        // CPU Temperature updates
        startTemperatureUpdates()

        // Storage Calculation
        val totalStorage = getTotalInternalMemorySize()
        val usedStorage = getUsedInternalMemorySize()
        binding.storageInfo.text = "${formatSize(usedStorage)} / ${formatSize(totalStorage)}"
        binding.storageProgressBar.progress = (usedStorage * 100 / totalStorage).toInt()

        // RAM Calculation
        val actManager = requireContext().getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager.getMemoryInfo(memInfo)
        val totalMemory = memInfo.totalMem
        val usedMemory = totalMemory - memInfo.availMem
        val percent = (usedMemory.toDouble() / totalMemory.toDouble() * 100).toInt()

        // Display RAM info
        binding.ramInfo.text = String.format(
            Locale.getDefault(),
            "%.2f GB / %.2f GB (RAM used)",
            usedMemory.toDouble() / (1024 * 1024 * 1024),
            totalMemory.toDouble() / (1024 * 1024 * 1024)
        )
        binding.ramProgressBar.progress = percent

        // ROM Space
        val romSpace = getRomSpace()
        binding.romSpace.text = "${formatSize(romSpace.first)} / ${formatSize(romSpace.second)}"

        // SD Card Space
        val sdCardSpace = getSdCardSpace()
        binding.sdCardSpace.text = "${formatSize(sdCardSpace.first)} / ${formatSize(sdCardSpace.second)}"

        // Fetch and display screen resolution
        val displayMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        binding.screenResolution.text = "${width} x ${height}"

// Fetch and display screen density
        val densityDpi = displayMetrics.densityDpi
        binding.screenDensity.text = "${densityDpi} DPI"

        // Check smallest screen width dp
        val smallestScreenWidthDp = resources.configuration.smallestScreenWidthDp
        Log.d("DeviceStatusFragment", "Smallest Screen Width DP: $smallestScreenWidthDp")

        // Determine if device supports multiple screens
        val supportsMultipleScreens = smallestScreenWidthDp >= 600
        binding.multipleScreen.text = if (supportsMultipleScreens) {
            "Supported"
        } else {
            "Unsupported"
        }

    }

    private fun getSdCardSpace(): Pair<Long, Long> {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            return Pair(0L, 0L)
        }

        val path = Environment.getExternalStorageDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        val totalBlocks = stat.blockCountLong

        return Pair(availableBlocks * blockSize, totalBlocks * blockSize)
    }

    private fun getRomSpace(): Pair<Long, Long> {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        val totalBlocks = stat.blockCountLong

        return Pair(availableBlocks * blockSize, totalBlocks * blockSize)
    }

    private fun getTotalInternalMemorySize(): Long {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        return totalBlocks * blockSize
    }

    private fun getUsedInternalMemorySize(): Long {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        return (stat.blockCountLong - availableBlocks) * blockSize
    }

    private fun formatSize(size: Long): String {
        var suffix: String
        val f = DecimalFormat("#.##")
        var sizeDouble = size.toDouble()

        if (size >= 1024) {
            suffix = "KB"
            sizeDouble /= 1024
            if (sizeDouble >= 1024) {
                suffix = "MB"
                sizeDouble /= 1024
                if (sizeDouble >= 1024) {
                    suffix = "GB"
                    sizeDouble /= 1024
                }
            }
        } else {
            suffix = "Bytes"
        }

        return f.format(sizeDouble) + " " + suffix
    }

    private fun startTemperatureUpdates() {
        val timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val cpuTemperatureString = fetchCPUTemperature()
                val cpuTemperature = cpuTemperatureString.toFloatOrNull() ?: 0f
                val temperatureInFahrenheit = (cpuTemperature * 9 / 5000) + 32

                handler.post {
                    binding.cpuInfo.text = String.format(Locale.getDefault(), "%.2f°F", temperatureInFahrenheit)
                    updateCPUProgressBar(temperatureInFahrenheit)
                }
            }
        }, 0, temperatureUpdateIntervalMs)
    }

    private fun updateCPUProgressBar(cpuTemperaturePercentage: Float) {
        val progress = (cpuTemperaturePercentage * 100 / 212).toInt() // Convert temperature to progress value
        if (progress >= 0 && progress <= 100) {
            binding.cpuProgressBar.progress = progress
        }
    }

    private fun fetchCPUTemperature(): String {
        val command = "cat sys/class/thermal/thermal_zone*/temp"
        val process = Runtime.getRuntime().exec(command)
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val temperature = reader.readLine()?.toFloatOrNull() ?: 0f
        reader.close()
        process.waitFor()
        return temperature.toString()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
