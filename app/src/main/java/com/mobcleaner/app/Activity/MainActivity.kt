package com.mobcleaner.app.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.mobcleaner.app.Fragment.BatterySaverFragment
import com.mobcleaner.app.Fragment.ProfileFragment
import com.mobcleaner.app.Fragment.ToolsFragment
import com.mobcleaner.app.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        navigationView.setItemIconTintList(null);
        //navigationView.setItemIconTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.transparent)));


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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.deviceStatus -> {
                val intent = Intent(this, DeviceStatusActivity::class.java)
                startActivity(intent)
            }
            R.id.gameBox -> {
                val intent = Intent(this, GameAssistantActivity::class.java)
                startActivity(intent)
            }
            R.id.settings -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            R.id.mailUs -> {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("support@example.com")) // Replace with your support email
                    putExtra(Intent.EXTRA_SUBJECT, "Support Request")
                }
                if (emailIntent.resolveActivity(this.packageManager) != null) {
                    startActivity(emailIntent)
                } else {
                    Toast.makeText(this, "No email client found", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.iLikeCleaner -> {
                val url = "https://play.google.com/store/apps/details?id=com.mobcleaner.app" // Replace this URL with your actual rate us URL

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }
        // Close the navigation drawer
        drawerLayout.closeDrawer(GravityCompat.END)
        return true
    }
}