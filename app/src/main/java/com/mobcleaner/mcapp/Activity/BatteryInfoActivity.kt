package com.mobcleaner.mcapp.Activity

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobcleaner.mcapp.databinding.ActivityBatteryInfoBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.util.*

class BatteryInfoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBatteryInfoBinding
    private val batteryTemperatureData = mutableListOf<Pair<Long, Float>>()
    private val batteryLevelData = mutableListOf<Pair<Long, Int>>()
    private val batteryVoltageData = mutableListOf<Pair<Long, Float>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        binding = ActivityBatteryInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        binding.backBtn.setOnClickListener {
            super.onBackPressed()
        }
        binding.appUsageBtn.setOnClickListener(){
            val intent=Intent(this,BatteryUsageActivity::class.java)
            startActivity(intent)
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
        binding.favName.text = getBatteryHealthStatus(batteryHealth)

        // Get and display battery capacity
        val batteryCapacity = getBatteryCapacity(this)
        binding.favName1.text = batteryCapacity

        val batteryVoltage = getBatteryVoltage(this)
        binding.favName3.text = batteryVoltage

        setUptempLineChart()
        generateBatteryTemperatureData()
        plotBatteryTemperatureData()
        generateDummyBatteryLevelData()
        generateBatteryVoltageData()
        plotBatteryLevelData()
        plotBatteryVoltageData()

        updateTemperatureInfo()
        updateVoltageinfo()
    }

    private fun updateVoltageinfo() {
        val latestVoltage = batteryVoltageData.lastOrNull()?.second ?: 0f
        val averageVoltage = if (batteryVoltageData.isNotEmpty()) {
            batteryVoltageData.map { it.second }.average().toFloat()
        } else {
            0f
        }

        binding.realTimeVoltage.text = "$latestVoltage V"
        binding.averageCurrent.text = "$averageVoltage V"
    }

    private fun setUptempLineChart() {
        binding.batteryTemperatureChart.apply {
            setDrawGridBackground(false)
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.WHITE
            axisLeft.textColor = Color.WHITE
            axisRight.isEnabled = false
            legend.isEnabled = false
        }
        binding.batteryLevelChart.apply {
            setDrawGridBackground(false)
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.WHITE
            axisLeft.textColor = Color.WHITE
            axisRight.isEnabled = false
            legend.isEnabled = false
        }

        binding.batteryVoltageChart.apply {
            setDrawGridBackground(false)
            description.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.textColor = Color.WHITE
            axisLeft.textColor = Color.WHITE
            axisRight.isEnabled = false
            legend.isEnabled = false
        }
    }
    private fun generateDummyBatteryLevelData() {
        // Simulate battery level data for demonstration
        val calendar = Calendar.getInstance()
        for (i in 0 until 10) {
            calendar.add(Calendar.HOUR, -1)
            val timestamp = calendar.timeInMillis
            val level = (20..100).random()
            batteryLevelData.add(Pair(timestamp, level))
        }
    }

    private fun generateBatteryVoltageData() {
        // Get battery voltage data
        val calendar = Calendar.getInstance()
        for (i in 0 until 10) {
            calendar.add(Calendar.HOUR, -1)
            val timestamp = calendar.timeInMillis
            val voltage = getBatteryVoltageInFloat(this)
            batteryVoltageData.add(Pair(timestamp, voltage))
        }
    }
    private fun getBatteryVoltageInFloat(context: Context): Float {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, ifilter)
        val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1

        return if (voltage != -1) {
            // Convert voltage from millivolts to volts
            voltage / 1000f
        } else {
            0f // Default to 0V if voltage not available
        }
    }

    private fun plotBatteryLevelData() {
        val entries = ArrayList<Entry>()
        batteryLevelData.forEachIndexed { index, pair ->
            entries.add(Entry(index.toFloat(), pair.second.toFloat()))
        }

        val dataSet = LineDataSet(entries, "Battery Level (%)")
        dataSet.color = Color.BLUE
        dataSet.setCircleColor(Color.BLUE)
        dataSet.lineWidth = 2f

        val lineData = LineData(dataSet)
        binding.batteryLevelChart.data = lineData
        binding.batteryLevelChart.invalidate() // Refresh chart
    }

    private fun plotBatteryVoltageData() {
        val entries = ArrayList<Entry>()
        batteryVoltageData.forEachIndexed { index, pair ->
            entries.add(Entry(index.toFloat(), pair.second))
        }

        val dataSet = LineDataSet(entries, "Battery Voltage (V)")
        dataSet.color = Color.GREEN
        dataSet.setCircleColor(Color.GREEN)
        dataSet.lineWidth = 2f

        val lineData = LineData(dataSet)
        binding.batteryVoltageChart.data = lineData
        binding.batteryVoltageChart.invalidate() // Refresh chart
    }
    private fun updateTemperatureInfo() {
        val latestTemp = batteryTemperatureData.lastOrNull()?.second ?: 0f
        val averageTemp = if (batteryTemperatureData.isNotEmpty()) {
            batteryTemperatureData.map { it.second }.average().toFloat()
        } else {
            0f
        }

        binding.realTimeTemp.text = "$latestTemp°C"
        binding.averageTemp.text = "$averageTemp°C"
    }
    private fun getBatteryVoltage(context: Context): String {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, ifilter)
        val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1

        return if (voltage != -1) {
            // Convert voltage from millivolts to volts
            val voltageInVolts = voltage / 1000f
            "${voltageInVolts} V"
        } else {
            "Battery voltage not available"
        }
    }
    private fun generateBatteryTemperatureData() {
        // Get battery temperature data
        val calendar = Calendar.getInstance()
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        for (i in 0 until 10) {
            calendar.add(Calendar.HOUR, -1)
            val timestamp = calendar.timeInMillis
            val temperature = getBatteryTemperature(this)
            batteryTemperatureData.add(Pair(timestamp, temperature))
        }
    }
    private fun getBatteryTemperature(context: Context): Float {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, ifilter)
        val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
        return if (temperature != -1) {
            // Convert temperature from tenths of a degree Celsius to degrees Celsius
            temperature / 10f
        } else {
            0f // Default to 0°C if temperature not available
        }
    }

    private fun plotBatteryTemperatureData() {
        val entries = ArrayList<Entry>()
        batteryTemperatureData.forEachIndexed { index, pair ->
            entries.add(Entry(index.toFloat(), pair.second))
        }

        val dataSet = LineDataSet(entries, "Battery Temperature")
        dataSet.color = Color.RED
        dataSet.setCircleColor(Color.RED)
        dataSet.lineWidth = 2f

        val lineData = LineData(dataSet)
        binding.batteryTemperatureChart.data = lineData
        binding.batteryTemperatureChart.invalidate() // Refresh chart
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
        return when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> 100
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> 50
            BatteryManager.BATTERY_HEALTH_DEAD -> 0
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> 75
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> 25
            BatteryManager.BATTERY_HEALTH_COLD -> 50
            else -> -1
        }
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
