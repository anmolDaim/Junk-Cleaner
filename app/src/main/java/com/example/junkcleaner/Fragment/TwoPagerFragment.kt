package com.example.junkcleaner.Fragment

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.junkcleaner.R

class TwoPagerFragment : Fragment() {
    private lateinit var batter_consu:TextView
    private lateinit var hoursTextView:TextView
    private lateinit var minutesTextView:TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
       val view:View=inflater.inflate(R.layout.fragment_two_pager,container,false)
        batter_consu=view.findViewById(R.id.batter_consu)
        hoursTextView=view.findViewById(R.id.hoursTextView)
        minutesTextView=view.findViewById(R.id.minutesTextView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get battery percentage
        val batteryPct = getBatteryPercentage(requireContext())
        batter_consu.text = "$batteryPct%"

        // Calculate battery time left
        val batteryTimeLeft = calculateBatteryTimeLeft(requireContext())
        hoursTextView.text = batteryTimeLeft.first.toString()
        minutesTextView.text = batteryTimeLeft.second.toString()
    }
    private fun getBatteryPercentage(context: Context): Int {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, ifilter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return (level * 100 / scale)
    }

    private fun calculateBatteryTimeLeft(context: Context): Pair<Int, Int> {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = (level.toFloat() / scale.toFloat() * 100).toInt()

        val dischargeRate = 10 // Assumed average discharge rate in percentage per hour

        val timeRemainingInHours = (100 - batteryPct) / dischargeRate
        val timeRemainingInMinutes = ((100 - batteryPct) % dischargeRate) * 60 / dischargeRate

        return Pair(timeRemainingInHours, timeRemainingInMinutes)
    }
}