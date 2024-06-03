package com.example.junkcleaner.Activity

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.junkcleaner.Adapter.gameAppAdapter
import com.example.junkcleaner.DataClass.gameAppDataClass
import com.example.junkcleaner.databinding.ActivityGameAssistantBinding

class GameAssistantActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameAssistantBinding
   // private val REQUEST_CODE_PERMISSION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameAssistantBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            finish()
        }

        fetchGamingApps()

    }


    private fun fetchGamingApps() {
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val gameApps = mutableListOf<gameAppDataClass>()
        apps.forEach { app ->
            if (isGameApp(app.packageName)) {
                val appName = app.loadLabel(packageManager).toString()
                val appIcon = packageManager.getApplicationIcon(app)
                val packageName = app.packageName
                gameApps.add(gameAppDataClass(appName, appIcon, packageName))
            }
        }
        Log.d("fetchGamingApps","Size of apps ${gameApps.size}")
        binding.gamesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.gamesRecyclerView.adapter = gameAppAdapter(gameApps){ gameApp ->
            openGame(gameApp.packageName)
        }
    }
    //it is to check whether the all apps are gaming apps or not
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

    //it is to open current application
    private fun openGame(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            startActivity(launchIntent)
        } else {
            Toast.makeText(this, "Unable to open app", Toast.LENGTH_SHORT).show()
        }
    }
}