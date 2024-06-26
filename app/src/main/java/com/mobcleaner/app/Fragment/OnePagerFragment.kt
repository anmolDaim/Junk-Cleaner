package com.mobcleaner.app.Fragment

import android.app.Activity
import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.mobcleaner.app.R
import com.mobcleaner.app.transition.animateProgress
import kotlinx.coroutines.*

class OnePagerFragment : Fragment() {

    private lateinit var batteryPercentageTextView: TextView
    private lateinit var memoryUsageTextView: TextView
    private lateinit var runningAppsTextView: TextView
    private lateinit var batteryChargeProgressBar: ProgressBar
    private lateinit var memoryUsedProgressBar: ProgressBar
    private lateinit var runningAppProgressBar: ProgressBar
    private lateinit var junkCleanerBtn: AppCompatButton

    // Coroutine scope for managing coroutines within the fragment lifecycle
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    companion object {
        private const val USAGE_STATS_PERMISSION_REQUEST = 1000 // Request code for usage stats permission
        private const val REQUEST_CODE_PERMISSIONS = 1001 // Request code for other necessary permissions
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_one_pager, container, false)
        runningAppsTextView = view.findViewById(R.id.runningAppsTextView)
        memoryUsageTextView = view.findViewById(R.id.memoryUsageTextView)
        batteryPercentageTextView = view.findViewById(R.id.batteryPercentageTextView)
        batteryChargeProgressBar = view.findViewById(R.id.batteryChargeProgressBar)
        memoryUsedProgressBar = view.findViewById(R.id.memoryUsedProgressBar)
        runningAppProgressBar = view.findViewById(R.id.runningAppProgressBar)
        junkCleanerBtn = view.findViewById(R.id.junkCleanBtn)
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for permissions before creating the fragment
        if (!hasUsageStatsPermission(requireContext())) {
            requestUsageStatsPermission()
        } else {
            // Permissions already granted, proceed with initialization
            initializeFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        junkCleanerBtn.setOnClickListener {
            val bottomSheetFragment = MyBottomSheetDialogFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }
    }

    private fun initializeFragment() {
        // Initialize UI components and fetch necessary data
        coroutineScope.launch {
            // Get battery percentage
            val batteryPct = withContext(Dispatchers.Default) { getBatteryPercentage(requireContext()) }
            batteryPercentageTextView.text = "$batteryPct%"
            batteryChargeProgressBar.animateProgress(batteryPct)

            // Get memory usage
            val memoryInfo = withContext(Dispatchers.Default) { getMemoryInfo(requireActivity()) }
            val totalMemory = memoryInfo.totalMem / (1024 * 1024) // Convert bytes to megabytes
            val availableMemory = memoryInfo.availMem / (1024 * 1024) // Convert bytes to megabytes
            val usedMemory = totalMemory - availableMemory
            val memoryUsagePercentage = (usedMemory.toDouble() / totalMemory * 100).toInt()
            memoryUsageTextView.text = "$memoryUsagePercentage%"
            memoryUsedProgressBar.animateProgress(memoryUsagePercentage)

            // Get running apps
            val runningApps = withContext(Dispatchers.Default) { getRunningApps(requireContext()) }
            runningAppsTextView.text = "${runningApps.size}"
            Log.d("runningAppSize", "running apps ${runningApps.size}")

            val packageManager = requireContext().packageManager
            val installedPackages = packageManager.getInstalledPackages(0)
            val maxAppCount = installedPackages.size
            runningAppProgressBar.max = maxAppCount // Set maximum for scale
            runningAppProgressBar.animateProgress(runningApps.size.coerceAtMost(maxAppCount)) // Set progress ensuring it does not exceed max.
        }
    }

    private suspend fun getMemoryInfo(activity: Activity): ActivityManager.MemoryInfo {
        val activityManager = activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo
    }

    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOpsManager.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivityForResult(intent, USAGE_STATS_PERMISSION_REQUEST)
            } else {
                // Handle the case where the USAGE_ACCESS_SETTINGS activity is not available
                Toast.makeText(
                    requireContext(),
                    "Usage access settings not available on this device",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            // Handle the case where the API level is below LOLLIPOP
            Toast.makeText(
                requireContext(),
                "Usage access settings not available on this device",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getBatteryPercentage(context: Context): Int {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, ifilter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return (level * 100 / scale)
    }

    private suspend fun getRunningApps(context: Context): List<String> = withContext(Dispatchers.Default) {
        val runningApps = mutableListOf<String>()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()
            val appList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, time - 1000 * 1000, time)
            for (usageStats in appList) {
                runningApps.add(usageStats.packageName)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val packages = context.packageManager.getInstalledPackages(0)
            for (pkg in packages) {
                val uid = pkg.applicationInfo.uid
                try {
                    appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, uid, pkg.packageName)
                    runningApps.add(pkg.packageName)
                } catch (e: Exception) {
                    // Ignore
                }
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val runningTasks = activityManager.appTasks
            for (task in runningTasks) {
                task.taskInfo.baseActivity?.let { runningApps.add(it.packageName) }
            }
        } else {
            val runningAppProcesses = activityManager.runningAppProcesses
            for (processInfo in runningAppProcesses) {
                runningApps.add(processInfo.processName)
            }
        }
        return@withContext runningApps
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == USAGE_STATS_PERMISSION_REQUEST) {
            if (hasUsageStatsPermission(requireContext())) {
                // Permission granted, initialize the fragment
                initializeFragment()
            } else {
                // Permission not granted, handle accordingly
                Toast.makeText(requireContext(), "Usage access permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel() // Cancel all coroutines when the fragment is destroyed to avoid memory leaks
    }
}
