package com.mobcleaner.mcapp.Activity

import android.Manifest
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobcleaner.mcapp.Adapter.gameAppAdapter
import com.mobcleaner.mcapp.DataClass.gameAppDataClass
import com.mobcleaner.mcapp.databinding.ActivityGameAssistantBinding
import java.io.File

class GameAssistantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameAssistantBinding
    private val REQUEST_CODE_PERMISSION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameAssistantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            super.onBackPressed()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Check if the device is running Android 11 (API level 30) or higher
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.QUERY_ALL_PACKAGES) != PackageManager.PERMISSION_GRANTED) {
                // If the QUERY_ALL_PACKAGES permission is not granted, request it from the user
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.QUERY_ALL_PACKAGES), REQUEST_CODE_PERMISSION)
            } else {
                // If the permission is already granted, proceed to fetch gaming apps
                //Toast.makeText(this, "already granted", Toast.LENGTH_SHORT).show()
                fetchGamingApps()
            }
        } else {
            // If the device is running a version of Android lower than 11, proceed to fetch gaming apps without checking for the permission
            fetchGamingApps()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()

                fetchGamingApps()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchGamingApps() {
        binding.progressBar.visibility = View.VISIBLE

        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val gameApps = mutableListOf<gameAppDataClass>()
        apps.forEach { app ->
            if (isGameApp(app.packageName)) {
                val appName = app.loadLabel(packageManager).toString()
                val appIcon = packageManager.getApplicationIcon(app)
                val packageName = app.packageName
                val installDate = getInstallDate(packageName)
                val gameSize = getAppSize(packageName)
                gameApps.add(gameAppDataClass(appName, appIcon,installDate,gameSize ,packageName))
            }
        }
        Log.d("fetchGamingApps","Size of apps ${gameApps.size}")
        binding.gamesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.gamesRecyclerView.adapter = gameAppAdapter(gameApps){ gameApp ->
            openGame(gameApp.packageName)
        }
        binding.progressBar.visibility = View.GONE

        // Check if gameApps list is empty
        if (gameApps.isEmpty()) {
            showNoGamingAppsDialog()
        }
    }
    private fun getInstallDate(packageName: String): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val firstInstallTime = packageInfo.firstInstallTime
            val dateFormat = android.text.format.DateFormat.getDateFormat(applicationContext)
            dateFormat.format(firstInstallTime)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("GameAssistantActivity", "Package not found: $packageName", e)
            "N/A"
        }
    }

    private fun getAppSize(packageName: String): String {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val appSize = packageInfo.applicationInfo.sourceDir.let { File(it).length() }
            android.text.format.Formatter.formatShortFileSize(applicationContext, appSize)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("GameAssistantActivity", "Package not found: $packageName", e)
            "N/A"
        }
        }

    private fun isGameApp(packageName: String): Boolean {
        return try {
            val info = packageManager.getApplicationInfo(packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                info.category == ApplicationInfo.CATEGORY_GAME
            } else {
                info.flags and ApplicationInfo.FLAG_IS_GAME == ApplicationInfo.FLAG_IS_GAME
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("GameAssistantActivity", "Package not found: $packageName", e)
            false
        }
    }

    private fun openGame(packageName: String) {
        binding.progressBar.visibility = View.VISIBLE
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            Toast.makeText(this, "Unable to open app", Toast.LENGTH_SHORT).show()
        }
        binding.progressBar.visibility = View.GONE
    }

    private fun showNoGamingAppsDialog() {
        AlertDialog.Builder(this)
            .setTitle("No Gaming Apps Found")
            .setMessage("There are no gaming apps installed on your device.")
            .setPositiveButton("OK") { _, _ ->
                navigateToMainActivity()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
