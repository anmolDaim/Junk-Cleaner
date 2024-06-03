package com.example.junkcleaner.Activity

import android.Manifest
import android.app.AppOpsManager
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.TrafficStats
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.junkcleaner.Adapter.NetworkTrafficAdapter
import com.example.junkcleaner.DataClass.networkTrafficDataClass
import com.example.junkcleaner.R
import com.example.junkcleaner.databinding.ActivityNetworkTrafficBinding
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NetworkTrafficActivity : AppCompatActivity() {

    private lateinit var binding:ActivityNetworkTrafficBinding
    private val REQUEST_NETWORK_STATE_PERMISSION = 123
    private val REQUEST_USAGE_ACCESS_PERMISSION = 456

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityNetworkTrafficBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener{
            super.onBackPressed()
        }

        // Check for permissions on activity start
        if (checkNetworkPermission()) {
            if (hasUsageStatsPermission()) {
                networkGraph()
                MobileWifiDataUsed()
                displayNetworkTraffic()
            } else {
                requestUsageStatsPermission()
            }
        } else {
            showNetworkPermissionDialog()
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
                networkGraph()
                MobileWifiDataUsed()
                displayNetworkTraffic()
            } else {
                Toast.makeText(this, "Usage access permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun networkGraph() {
        // Get total mobile data usage since device boot
        val mobileDataUsage = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()

        // Get total Wi-Fi data usage since device boot
        val wifiDataUsage = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes() - mobileDataUsage

        // Display bar graph
        displayBarGraph(mobileDataUsage.toDouble(), wifiDataUsage.toDouble())
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
            valuesOnTopColor = ContextCompat.getColor(this@NetworkTrafficActivity, R.color.black)
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
            gridLabelRenderer.horizontalAxisTitle = "Data Type"
            gridLabelRenderer.verticalAxisTitle = "Data Usage (GB)"
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
        binding.networkGraph.gridLabelRenderer.horizontalLabelsColor = Color.WHITE
        binding.networkGraph.gridLabelRenderer.verticalLabelsColor = Color.WHITE
    }

    private fun MobileWifiDataUsed(){
        val mobileDataUsage = TrafficStats.getMobileRxBytes() + TrafficStats.getMobileTxBytes()
        val wifiDataUsage = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes() - mobileDataUsage

        val mobileDataUsageGB = mobileDataUsage.toDouble() / (1024 * 1024 * 1024)
        val wifiDataUsageGB = wifiDataUsage.toDouble() / (1024 * 1024 * 1024)

        binding.mobileData.text = String.format("%.2f GB", mobileDataUsageGB)
        binding.wifiData.text = String.format("%.2f GB", wifiDataUsageGB)
    }


    private fun displayNetworkTraffic() {
        Log.d("applist","enter int this method")

        binding.progressBar4.visibility=View.VISIBLE
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val networkStatsManager = getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -1) // Last 24 hours

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

            val uid = appInfo.uid
            val mobileDataUsage = getNetworkUsageForUid(networkStatsManager, uid, ConnectivityManager.TYPE_MOBILE, startTime, endTime)
            val wifiDataUsage = getNetworkUsageForUid(networkStatsManager, uid, ConnectivityManager.TYPE_WIFI, startTime, endTime)

            val totalDataUsage = mobileDataUsage + wifiDataUsage
            val totalDataUsageStr =when {
                totalDataUsage >= 1024 * 1024 * 1024 -> String.format("%.2f GB", totalDataUsage.toDouble() / (1024 * 1024 * 1024))
                totalDataUsage >= 1024 * 1024 -> String.format("%.2f MB", totalDataUsage.toDouble() / (1024 * 1024))
                totalDataUsage >= 1024 -> String.format("%.2f KB", totalDataUsage.toDouble() / 1024)
                else -> "$totalDataUsage B"
            }

            // Convert to percentage (0-100)
            val mobileDataUsagePercentage = if (totalDataUsage != 0L) ((mobileDataUsage.toDouble() / totalDataUsage) * 100).toInt() else 0
            val wifiDataUsagePercentage = if (totalDataUsage != 0L) ((wifiDataUsage.toDouble() / totalDataUsage) * 100).toInt() else 0

            appDataList.add(
                networkTrafficDataClass(
                    appName = appName,
                    dataUsage = totalDataUsageStr,
                    icon = icon,
                    mobileDataUsage = mobileDataUsagePercentage,
                    wifiDataUsage = wifiDataUsagePercentage
                )
            )
        }

        // Log the size of the list
        Log.d("applist", "Size of appDataList: ${appDataList.size}")

        binding.networkTrafficRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.networkTrafficRecyclerView.adapter = NetworkTrafficAdapter(appDataList)

        binding.progressBar4.visibility=View.GONE
    }

    private fun getNetworkUsageForUid(networkStatsManager: NetworkStatsManager, uid: Int, networkType: Int, startTime: Long, endTime: Long): Long {
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
        return totalBytes
    }

}