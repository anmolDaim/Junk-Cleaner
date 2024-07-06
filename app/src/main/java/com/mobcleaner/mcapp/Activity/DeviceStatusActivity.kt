package com.mobcleaner.mcapp.Activity

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.mobcleaner.mcapp.Fragment.DeviceStatusFragment
import com.mobcleaner.mcapp.R
import com.mobcleaner.mcapp.databinding.ActivityDeviceStatusBinding


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
//           val intent= Intent(this,MainActivity::class.java)
//            startActivity(intent)
            super.onBackPressed()
        }

        // Display initial fragment or perform initial action
        supportFragmentManager.beginTransaction().replace(
            R.id.DrawarContainer,
            DeviceStatusFragment()
        ).commit()

    }


}