package com.example.junkcleaner.Fragment

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.example.junkcleaner.R


class OnePagerFragment : Fragment() {

    private lateinit var batteryPercentageTextView:TextView
    private lateinit var memoryUsageTextView:TextView
    private lateinit var runningAppsTextView:TextView
    private lateinit var batteryChargeProgressBar:ProgressBar
    private lateinit var memoryUsedProgressBar:ProgressBar
    private lateinit var runningAppProgressBar:ProgressBar
    private lateinit var junkCleanerBtn:AppCompatButton



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view:View=inflater.inflate(R.layout.fragment_one_pager,container,false)
        runningAppsTextView=view.findViewById(R.id.runningAppsTextView)
        memoryUsageTextView=view.findViewById(R.id.memoryUsageTextView)
        batteryPercentageTextView=view.findViewById(R.id.batteryPercentageTextView)
        batteryChargeProgressBar=view.findViewById(R.id.batteryChargeProgressBar)
        memoryUsedProgressBar=view.findViewById(R.id.memoryUsedProgressBar)
        runningAppProgressBar=view.findViewById(R.id.runningAppProgressBar)
        junkCleanerBtn=view.findViewById(R.id.junkCleanBtn)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get battery percentage
        val batteryPct = getBatteryPercentage(requireContext())
        batteryPercentageTextView.text = "$batteryPct%"
        batteryChargeProgressBar.progress = batteryPct

        // Get memory usage
        val memoryInfo = ActivityManager.MemoryInfo()
        val activityManager = requireActivity().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.getMemoryInfo(memoryInfo)
        val totalMemory = memoryInfo.totalMem / (1024 * 1024) // Convert bytes to megabytes
        val availableMemory = memoryInfo.availMem / (1024 * 1024) // Convert bytes to megabytes
        val usedMemory = totalMemory - availableMemory
        val memoryUsagePercentage = (usedMemory.toDouble() / totalMemory * 100).toInt()
        memoryUsageTextView.text = "$memoryUsagePercentage%"
        memoryUsedProgressBar.progress = memoryUsagePercentage

        // Get running apps
        val runningAppProcesses = activityManager.runningAppProcesses
        val runningAppsCount = runningAppProcesses.size
        runningAppsTextView.text = "$runningAppsCount"
        runningAppsTextView.text = "$runningAppsCount"

        val maxAppCount = 100 // This is arbitrary and could be changed based on expected running apps.
        runningAppProgressBar.max = maxAppCount // Set maximum for scale
        runningAppProgressBar.progress = runningAppsCount.coerceAtMost(maxAppCount) // Set progress ensuring it does not exceed max.

//
//        optimizeBtn.setOnClickListener {
//            val bottomSheet = OptimizeBottomSheetDialogFragment()
//            bottomSheet.show(childFragmentManager, bottomSheet.tag)
//        }

        junkCleanerBtn.setOnClickListener(){
            val bottomSheetFragment = MyBottomSheetDialogFragment()
            bottomSheetFragment.show(parentFragmentManager, bottomSheetFragment.tag)
        }
    }

    private fun getBatteryPercentage(context: Context): Int {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, ifilter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return (level * 100 / scale)
    }

}