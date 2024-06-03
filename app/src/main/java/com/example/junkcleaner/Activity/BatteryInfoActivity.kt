package com.example.junkcleaner.Activity

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.junkcleaner.databinding.ActivityBatteryInfoBinding
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.Date
import java.util.Random



class BatteryInfoActivity : AppCompatActivity() {


    private lateinit var binding: ActivityBatteryInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityBatteryInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener{
            super.onBackPressed()
        }

        // Get battery percentage
        val batteryPct = getBatteryPercentage(this)
        binding.batterConsu.text = "$batteryPct%"

        // Calculate battery time left
        val batteryTimeLeft = calculateBatteryTimeLeft(this)
        binding.hoursTextView.text = batteryTimeLeft.first.toString()
        binding.minutesTextView.text = batteryTimeLeft.second.toString()

        // Get battery health and update UI
        val batteryHealth = getBatteryHealth(this)
//        binding.favName.text = "$batteryHealth%"
        binding.favName.text = getBatteryHealthStatus(batteryHealth)


        // Get and display battery capacity
        val batteryCapacity = getBatteryCapacity(this)
        binding.favName1.text = batteryCapacity


        val batteryVoltage = getBatteryVoltage(this)
        binding.favName3.text = batteryVoltage
        // Assuming you have references to the graph views
        val batteryLevelGraph = binding.batteryLevelChart
        val batteryTemperatureGraph = binding.batteryTemperatureChart
        val batteryVoltageGraph = binding.batteryVoltageChart

        // Generate dummy data for the graphs
        val random = Random()
        val batteryLevelSeries = LineGraphSeries<DataPoint>()
        val batteryTemperatureSeries = LineGraphSeries<DataPoint>()
        val batteryVoltageSeries = LineGraphSeries<DataPoint>()

        // Populate the series with dummy data
        for (i in 0..100) {
            val x = i.toDouble()
            val y1 = random.nextDouble() * 100
            val y2 = random.nextDouble() * 50
            val y3 = random.nextDouble() * 20

            batteryLevelSeries.appendData(DataPoint(x, y1), true, 101)
            batteryTemperatureSeries.appendData(DataPoint(x, y2), true, 101)
            batteryVoltageSeries.appendData(DataPoint(x, y3), true, 101)
        }
// Set color for each series
        batteryLevelSeries.color = Color.BLUE
        batteryTemperatureSeries.color = Color.RED
        batteryVoltageSeries.color = Color.GREEN

        // Set title for each graph
        batteryLevelGraph.title = "Battery Level (%)"
        batteryLevelGraph.titleColor = Color.WHITE
        batteryTemperatureGraph.title = "Battery Temperature (°C)"
        batteryTemperatureGraph.titleColor = Color.WHITE
        batteryVoltageGraph.title = "Battery Voltage (mV)"
        batteryVoltageGraph.titleColor = Color.WHITE

        // Add series to the graphs
        batteryLevelGraph.addSeries(batteryLevelSeries)
        batteryTemperatureGraph.addSeries(batteryTemperatureSeries)
        batteryVoltageGraph.addSeries(batteryVoltageSeries)

        // Set viewport for each graph
        batteryLevelGraph.viewport.isScalable = true
        batteryTemperatureGraph.viewport.isScalable = true
        batteryVoltageGraph.viewport.isScalable = true

        // Optional: Set manual X bounds
        batteryLevelGraph.viewport.setMinX(0.0)
        batteryLevelGraph.viewport.setMaxX(100.0)
        batteryTemperatureGraph.viewport.setMinX(0.0)
        batteryTemperatureGraph.viewport.setMaxX(100.0)
        batteryVoltageGraph.viewport.setMinX(0.0)
        batteryVoltageGraph.viewport.setMaxX(100.0)

        batteryLevelGraph.gridLabelRenderer.horizontalLabelsColor = Color.WHITE
        batteryLevelGraph.gridLabelRenderer.verticalLabelsColor = Color.WHITE
        batteryTemperatureGraph.gridLabelRenderer.horizontalLabelsColor = Color.WHITE
        batteryTemperatureGraph.gridLabelRenderer.verticalLabelsColor = Color.WHITE
        batteryVoltageGraph.gridLabelRenderer.horizontalLabelsColor = Color.WHITE
    }



    private fun getBatteryVoltage(context: Context): String {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, ifilter)
        val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1

        return if (voltage != -1) {
            // Convert voltage from millivolts to volts
            val voltageInVolts = voltage / 1000f
            "${voltageInVolts} mV"
        } else {
            "Battery voltage not available"
        }
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
    private fun getBatteryHealth(context: Context): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val health = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN) ?: BatteryManager.BATTERY_HEALTH_UNKNOWN
        val batteryHealthPercentage = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> 100
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> 50
            BatteryManager.BATTERY_HEALTH_DEAD -> 0
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 75
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> 25
            BatteryManager.BATTERY_HEALTH_COLD -> 50
            else -> -1
        }
        return batteryHealthPercentage
    }

    private fun getBatteryHealthStatus(healthPercentage: Int): String {
        return when {
            healthPercentage == 100 -> "Good"
            healthPercentage in 75..99 -> "Moderate"
            healthPercentage in 50..74 -> "Poor"
            healthPercentage in 25..49 -> "Bad"
            healthPercentage in 0..24 -> "Very Bad"
            else -> "Unknown"
        }
    }
    private fun getBatteryCapacity(context: Context): String {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        // First, try to get battery capacity using BatteryManager
        val totalCapacity = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        if (totalCapacity != -1L) {
            val batteryPct = getBatteryPercentage(context)
            val currentCapacity = totalCapacity * batteryPct / 100
            return "$currentCapacity/$totalCapacity mAh"
        }

        // If BatteryManager doesn't provide capacity, fall back to reading from system files
        val estimatedCapacity = estimateBatteryCapacity()
        if (estimatedCapacity != -1) {
            val batteryPct = getBatteryPercentage(context)
            val currentCapacity = estimatedCapacity * batteryPct / 100
            return "$currentCapacity/$estimatedCapacity mAh"
        }

        return "Battery capacity not available"
    }

    private fun estimateBatteryCapacity(): Int {
        val path = "/sys/class/power_supply/battery/charge_full"
        return try {
            val reader = BufferedReader(FileReader(File(path)))
            val line = reader.readLine()
            reader.close()
            val capacityInUah = line.toInt()
            capacityInUah / 1000
        } catch (e: IOException) {
            e.printStackTrace()
            -1 // Return -1 if the capacity couldn't be read
        }
    }
}