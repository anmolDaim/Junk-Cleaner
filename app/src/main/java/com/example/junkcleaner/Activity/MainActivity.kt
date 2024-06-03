package com.example.junkcleaner.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.junkcleaner.Fragment.BatterySaverFragment
import com.example.junkcleaner.Fragment.ProfileFragment
import com.example.junkcleaner.R
import com.example.junkcleaner.Fragment.ToolsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        bottomNavigationView=findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnNavigationItemSelectedListener ( navListener )

        // Display initial fragment or perform initial action
        supportFragmentManager.beginTransaction().replace(
            R.id.container,
            BatterySaverFragment()
        ).commit()
    }
    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        var selectedFragment: Fragment? = null
        when (item.itemId) {
            R.id.navigation_battery_saver -> selectedFragment = BatterySaverFragment()
            R.id.navigation_tools -> selectedFragment = ToolsFragment()
            R.id.navigation_profile -> selectedFragment = ProfileFragment()
        }
        // Replace the current fragment with the selected one
        if (selectedFragment != null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, selectedFragment)
                .commit()
            return@OnNavigationItemSelectedListener true
        }
        false
    }
}