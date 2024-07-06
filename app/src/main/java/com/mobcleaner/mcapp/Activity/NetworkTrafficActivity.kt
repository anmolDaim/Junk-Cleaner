package com.mobcleaner.mcapp.Activity

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobcleaner.mcapp.Adapter.NetworkTrafficAdapter
import com.mobcleaner.mcapp.DataClass.networkTrafficDataClass
import com.mobcleaner.mcapp.R
import com.mobcleaner.mcapp.databinding.ActivityNetworkTrafficBinding
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class NetworkTrafficActivity : AppCompatActivity(),CoroutineScope {

    private lateinit var binding:ActivityNetworkTrafficBinding
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private lateinit var adapter: NetworkTrafficAdapter
    private val REQUEST_NETWORK_STATE_PERMISSION = 123
    private val REQUEST_USAGE_ACCESS_PERMISSION = 456
    private var previousMobileDataUsage = 0L
    private var previousWifiDataUsage = 0L

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancel all coroutines when the activity is destroyed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        binding=ActivityNetworkTrafficBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        binding.backBtn.setOnClickListener{
            super.onBackPressed()
        }

        // Check for permissions on activity start
        if (checkNetworkPermission()) {
            if (hasUsageStatsPermission()) {
                launchDataUsageTasks()
            } else {
                requestUsageStatsPermission()
            }
        } else {
            showNetworkPermissionDialog()
        }

    }

    private fun launchDataUsageTasks() {
        launch {
            //binding.animationview.visibility = View.VISIBLE
            networkGraph()
            MobileWifiDataUsed()
            displayNetworkTraffic()
            //binding.animationview.visibility = View.GONE
        }
    }


    private fun checkNetworkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestNetworkPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_NETWORK_STATE),
            REQUEST_NETWORK_STATE_PERMISSION
        )
    }

    private fun showNetworkPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Network Permission")
            .setMessage("Grant permission to access Network related details!")
            .setPositiveButton("Allow") { _, _ ->
                requestNetworkPermission()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun requestUsageStatsPermission() {
        AlertDialog.Builder(this)
            .setTitle("Usage Access Permission")
            .setMessage("Grant usage access to view data usage statistics!")
            .setPositiveButton("Allow") { _, _ ->
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:$packageName")
                }
                startActivityForResult(intent, REQUEST_USAGE_ACCESS_PERMISSION)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_USAGE_ACCESS_PERMISSION) {
            if (hasUsageStatsPermission()) {
                launchDataUsageTasks()
            } else {
                Toast.makeText(this, "Usage access permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private suspend fun networkGraph() = withContext(Dispatchers.IO) {
        // Get total mobile data usage since device boot
        val mobileDataUsage = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()

        // Get total Wi-Fi data usage since device boot
        val wifiDataUsage = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes() - mobileDataUsage

        // Display bar graph on the main thread
        withContext(Dispatchers.Main) {
            displayBarGraph(mobileDataUsage.toDouble(), wifiDataUsage.toDouble())
        }
    }


    private fun displayBarGraph(mobileDataUsage: Double, wifiDataUsage: Double) {
        val series = BarGraphSeries(
            arrayOf(
                DataPoint(0.0, mobileDataUsage / (1024 * 1024 * 1024)), // Convert to GB
                DataPoint(1.0, wifiDataUsage / (1024 * 1024 * 1024))    // Convert to GB
            )
        )

        // Customize the series
        series.apply {
            isDrawValuesOnTop = true
            valuesOnTopColor = ContextCompat.getColor(this@NetworkTrafficActivity, R.color.white)
            spacing = 50
        }

        // Set bar colors
        val mobileDataColor = ContextCompat.getColor(this, R.color.yellow)
        val wifiDataColor = ContextCompat.getColor(this, R.color.blue)
        series.setValueDependentColor { data ->
            if (data.x == 0.0) mobileDataColor else wifiDataColor
        }

        binding.networkGraph.apply {
            removeAllSeries()
            addSeries(series)

            // Customize viewport
            viewport.isXAxisBoundsManual = true
            viewport.setMinX(-0.5)
            viewport.setMaxX(1.5)
            viewport.isYAxisBoundsManual = true
            viewport.setMinY(0.0)

            // Set labels for X-axis (0 = Mobile, 1 = Wi-Fi)
//            gridLabelRenderer.horizontalAxisTitle = "Data Type"
//            gridLabelRenderer.verticalAxisTitle = "Data Usage (GB)"
            gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
                override fun formatLabel(value: Double, isValueX: Boolean): String {
                    return if (isValueX) {
                        if (value == 0.0) "Mobile" else "Wi-Fi"
                    } else {
                        super.formatLabel(value, isValueX)
                    }
                }
            }

            // Set custom label formatter for Y-axis (GB)
            gridLabelRenderer.setLabelFormatter(object : DefaultLabelFormatter() {
                override fun formatLabel(value: Double, isValueX: Boolean): String {
                    return if (!isValueX) {
                        String.format("%.2f GB", value)
                    } else {
                        super.formatLabel(value, isValueX)
                    }
                }
            })
        }
        binding.networkGraph.gridLabelRenderer.horizontalLabelsColor = R.color.white
        binding.networkGraph.gridLabelRenderer.verticalLabelsColor = R.color.white
    }

    private suspend fun MobileWifiDataUsed() = withContext(Dispatchers.IO) {
        val mobileRxBytes = TrafficStats.getMobileRxBytes()
        val mobileTxBytes = TrafficStats.getMobileTxBytes()
        val mobileDataUsage = mobileRxBytes + mobileTxBytes

        val wifiRxBytes = TrafficStats.getTotalRxBytes() - mobileRxBytes
        val wifiTxBytes = TrafficStats.getTotalTxBytes() - mobileTxBytes
        val wifiDataUsage = wifiRxBytes + wifiTxBytes

        withContext(Dispatchers.Main) {
            binding.mobileData.text = formatDataUsage(mobileDataUsage)
            binding.wifiData.text = formatDataUsage(wifiDataUsage)
        }
    }


    private suspend fun displayNetworkTraffic() = withContext(Dispatchers.IO) {
        Log.d("applist", "enter int this method")
        binding.animationview.visibility = View.VISIBLE
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val networkStatsManager = getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -7) // Last 24 hours

        val endTime = System.currentTimeMillis()
        val startTime = calendar.timeInMillis

        val queryUsageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, startTime, endTime
        )

        val packageManager = packageManager
        val appDataList = mutableListOf<networkTrafficDataClass>()

        for (usageStats in queryUsageStats) {
            val appInfo: ApplicationInfo?
            try {
                appInfo = packageManager.getApplicationInfo(usageStats.packageName, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                continue
            }
            val icon: Drawable = packageManager.getApplicationIcon(appInfo)
            val appName = packageManager.getApplicationLabel(appInfo).toString()

            // Calculate total data usage for the last 7 days
            val totalDataUsage = getTotalDataUsageForApp(networkStatsManager, appInfo.uid, startTime, endTime)
Log.d("totalDataUsage","total data usage: $totalDataUsage")
            val totalDataUsageStr = formatDataUsage(totalDataUsage)
            Log.d("totalDataUsage","total data usage String: $totalDataUsageStr")
            // Convert to percentage (0-100)
            val mobileDataUsagePercentage = calculatePercentage(
                getNetworkUsageForUid(networkStatsManager, appInfo.uid, ConnectivityManager.TYPE_MOBILE, startTime, endTime),
                totalDataUsage
            )
            val wifiDataUsagePercentage = calculatePercentage(
                getNetworkUsageForUid(networkStatsManager, appInfo.uid, ConnectivityManager.TYPE_WIFI, startTime, endTime),
                totalDataUsage
            )
            appDataList.add(
                networkTrafficDataClass(
                    appName = appName,
                    dataUsage = totalDataUsageStr,
                    icon = icon,
                    mobileDataUsage = mobileDataUsagePercentage,
                    wifiDataUsage = wifiDataUsagePercentage,
                    packageName=appInfo.packageName
                )
            )
        }

//        // Sort appDataList by total data usage descending
//        appDataList.sortByDescending { it.dataUsage }

        // Log the size of the list
        Log.d("applist", "Size of appDataList: ${appDataList.size}")

        withContext(Dispatchers.Main) {
            binding.networkTrafficRecyclerView.layoutManager = LinearLayoutManager(this@NetworkTrafficActivity)
            binding.networkTrafficRecyclerView.adapter = NetworkTrafficAdapter(appDataList,this@NetworkTrafficActivity)
            binding.animationview.visibility = View.GONE
        }
    }

    private fun calculatePercentage(dataUsage: Long, totalUsage: Long): Int {
        return if (totalUsage != 0L) ((dataUsage.toDouble() / totalUsage) * 100).toInt() else 0
    }
    private suspend fun getTotalDataUsageForApp(
        networkStatsManager: NetworkStatsManager,
        uid: Int,
        startTime: Long,
        endTime: Long
    ): Long {
        var totalBytes = 0L
        val networkTypes = intArrayOf(ConnectivityManager.TYPE_MOBILE, ConnectivityManager.TYPE_WIFI)

        for (networkType in networkTypes) {
            totalBytes += getNetworkUsageForUid(networkStatsManager, uid, networkType, startTime, endTime)
        }

        return totalBytes
    }


    private suspend fun getNetworkUsageForUid(
        networkStatsManager: NetworkStatsManager,
        uid: Int,
        networkType: Int,
        startTime: Long,
        endTime: Long
    ): Long = withContext(Dispatchers.IO) {
        var totalBytes = 0L
        val networkStats = networkStatsManager.querySummary(networkType, null, startTime, endTime)
        val bucket = NetworkStats.Bucket()
        while (networkStats.hasNextBucket()) {
            networkStats.getNextBucket(bucket)
            if (bucket.uid == uid) {
                totalBytes += bucket.rxBytes + bucket.txBytes
            }
        }
        networkStats.close()
        totalBytes
    }
    private fun formatDataUsage(dataUsageBytes: Long): String {
        return when {
            dataUsageBytes >= 1024 * 1024 * 1024 -> String.format("%.2f GB", dataUsageBytes.toDouble() / (1024 * 1024 * 1024))
            dataUsageBytes >= 1024 * 1024 -> String.format("%.2f MB", dataUsageBytes.toDouble() / (1024 * 1024))
            dataUsageBytes >= 1024 -> String.format("%.2f KB", dataUsageBytes.toDouble() / 1024)
            else -> "$dataUsageBytes B"
        }
    }


}