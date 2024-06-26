package com.mobcleaner.app.Activity

import android.Manifest
import java.text.SimpleDateFormat
import java.util.Date
import android.app.usage.StorageStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.storage.StorageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobcleaner.app.Adapter.AppManagerAdapter
import com.mobcleaner.app.Adapter.itemAppManagerAdapter
import com.mobcleaner.app.DataClass.appManagerDataClass
import com.mobcleaner.app.DataClass.itemAppManagerDataClass
import com.mobcleaner.app.R
import com.mobcleaner.app.databinding.ActivityAppManagerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import kotlin.coroutines.CoroutineContext

class AppManagerActivity : AppCompatActivity(),
    itemAppManagerAdapter.OnCheckboxChangeListener,
    itemAppManagerAdapter.DetailsClickListner,
    CoroutineScope {
    private lateinit var binding: ActivityAppManagerBinding
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private lateinit var packageManager: PackageManager
    private val PERMISSION_REQUEST_CODE = 100
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var storageStatsManager: StorageStatsManager
    private lateinit var storageManager: StorageManager
    private lateinit var appList: MutableList<itemAppManagerDataClass>
    private lateinit var adapter: itemAppManagerAdapter



    // SharedPreferences to store the permissions state
    private val PREFS_NAME = "AppManagerPrefs"
    private val PERMISSIONS_GRANTED = "permissions_granted"


    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancel all coroutines when the activity is destroyed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        packageManager = applicationContext.packageManager

        usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        storageStatsManager = getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        storageManager = getSystemService(Context.STORAGE_SERVICE) as StorageManager


        binding.backBtn.setOnClickListener {
            finish()
        }

        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val permissionsGranted = sharedPreferences.getBoolean(PERMISSIONS_GRANTED, false)

        // Check if permissions are granted, if not, request them
        if (!permissionsGranted) {
            showPermissionExplanationDialog()
        } else {
            // Permissions are already granted, proceed with fetching app details
            fetchAppDetails()
        }
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_PACKAGE_SIZE) != PackageManager.PERMISSION_GRANTED
//            || ContextCompat.checkSelfPermission(this, Manifest.permission.PACKAGE_USAGE_STATS) != PackageManager.PERMISSION_GRANTED
//            || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            showPermissionExplanationDialog()
//        } else {
//            fetchAppDetails()
//        }

        val catArr = ArrayList<appManagerDataClass>()
        catArr.add(appManagerDataClass("Name"))
        catArr.add(appManagerDataClass("Installation"))
        catArr.add(appManagerDataClass("Size"))

        val linearLayout = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.appManagerCategoryRecyclerView.layoutManager = linearLayout
        val CategoryAdapter = AppManagerAdapter(this, catArr) { categoryName ->
            when (categoryName) {
                "Name" -> fetchName()
                "Installation" -> fetchInstallation()
                "Size" -> fetchSize()
            }
        }

        binding.appManagerCategoryRecyclerView.adapter = CategoryAdapter


    }


    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app needs permissions to access app details like size, installation date, and last used time. Please grant these permissions to proceed.")
            .setPositiveButton("Allow") { _, _ ->
                requestPermissions()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                val intent=Intent(this,MainActivity()::class.java)
                startActivity(intent)
            }
            .create()
            .show()
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.GET_PACKAGE_SIZE,
                Manifest.permission.PACKAGE_USAGE_STATS,
               // Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                savePermissionsState(true)
                fetchAppDetails()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Permission Denied")
                    .setMessage("Without these permissions, the app cannot function properly. Please grant the permissions from settings.")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
        }
    }

    private fun savePermissionsState(granted: Boolean) {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(PERMISSIONS_GRANTED, granted)
        editor.apply()
    }

    private fun fetchAppDetails() {
        fetchName()
    }

    private fun fetchName() {

        Log.d("AppManagerActivity", "Fetching installed app names...")
        binding.progressBar.visibility= View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val apps = withContext(Dispatchers.IO) {
                    packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                }
                appList = mutableListOf()

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                withContext(Dispatchers.IO) {
                    apps.forEach { app ->
                        val appName = app.loadLabel(packageManager).toString()
                        val appIcon = packageManager.getApplicationIcon(app)
                        val packageName = app.packageName

                        val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        val packageInfo = packageManager.getPackageInfo(packageName, 0)
                        val appInstall = sdf.format(Date(packageInfo.firstInstallTime))

                        val appDir = app.sourceDir
                        val appFile = File(appDir)
                        val appSize = (appFile.length() / (1024 * 1024)).toString() + " MB"

                        appList.add(
                            itemAppManagerDataClass(
                                appName,
                                appIcon,
                                packageName,
                                if (isSystemApp) View.GONE else View.VISIBLE,
                                appSize,
                                View.VISIBLE,
                                appInstall,
                                View.VISIBLE
                            )
                        )
                    }
                }

                binding.appManagerItemRecyclerView.layoutManager = LinearLayoutManager(this@AppManagerActivity)
                binding.appManagerItemRecyclerView.adapter = itemAppManagerAdapter(appList, this@AppManagerActivity, this@AppManagerActivity)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onCheckboxChanged() {
        val anyChecked = appList.any { it.isSelected }
        if (anyChecked) {
            binding.uninstallButton.setBackgroundResource(R.drawable.optimize_btn)
            binding.backupBtn.setBackgroundResource(R.drawable.optimize_btn)
        } else {
            binding.uninstallButton.setBackgroundResource(R.drawable.remove_btn)
            binding.backupBtn.setBackgroundResource(R.drawable.remove_btn)
        }
        binding.uninstallButton.setOnClickListener {
            if (anyChecked) {
                showDeleteConfirmationDialog()
            } else {
                Toast.makeText(this, "No items selected for deletion", Toast.LENGTH_SHORT).show()
            }
        }
        binding.backupBtn.setOnClickListener{
            if (anyChecked) {
                showBackupConfirmationDialog()
            } else {
                Toast.makeText(this, "No items selected for deletion", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showBackupConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Backup")
            .setMessage("Selected apps will be backed up.")
            .setPositiveButton("BACKUP") { _, _ ->
                performBackup()
            }
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
    private fun performBackup() {
        val selectedItems = appList.filter { it.isSelected }
        for (item in selectedItems) {
            backupApp(item.packageName, item.appName)
        }
    }

    private fun showDeleteConfirmationDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Uninstall")
        alertDialogBuilder.setMessage("Uninstall application, are you sure?")

        alertDialogBuilder.setPositiveButton("UNINSTALL") { dialog, _ ->
            deleteSelectedItems()
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun deleteSelectedItems() {
        val selectedItems = appList.filter { it.isSelected }
        if (selectedItems.isNotEmpty()) {
            for (item in selectedItems) {
                Log.d("AppManagerActivity", "Deleting: ${item.appName}")
                uninstallApp(item.packageName)
            }

            // Add a delay to allow for the uninstallation process to complete
            Handler(Looper.getMainLooper()).postDelayed({
                navigateToMainActivity()
            }, 3000) // Adjust the delay time as necessary (e.g., 5000 milliseconds = 5 seconds)
        } else {
            Toast.makeText(this, "No items selected for deletion", Toast.LENGTH_SHORT).show()
        }
    }
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun uninstallApp(packageName: String?) {
        if (packageName == null || packageName.isEmpty()) return
        val intent = Intent(Intent.ACTION_DELETE)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)

    }

    private fun fetchInstallation() {
        Log.d("AppManagerActivity", "Fetching installed app names...")
        binding.progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val apps = withContext(Dispatchers.IO) {
                    packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                }
                appList = mutableListOf()

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                withContext(Dispatchers.IO) {
                    apps.forEach { app ->
                        val appName = app.loadLabel(packageManager).toString()
                        val appIcon = packageManager.getApplicationIcon(app)
                        val packageName = app.packageName

                        val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                        if (!isSystemApp) {
                            val packageInfo = packageManager.getPackageInfo(packageName, 0)
                            val appInstall = sdf.format(Date(packageInfo.firstInstallTime))

                            val appDir = app.sourceDir
                            val appFile = File(appDir)
                            val appSize = (appFile.length() / (1024 * 1024)).toString() + " MB"

                            appList.add(
                                itemAppManagerDataClass(
                                    appName,
                                    appIcon,
                                    packageName,
                                    View.VISIBLE,
                                    appSize,
                                    View.VISIBLE,
                                    appInstall,
                                    View.VISIBLE
                                )
                            )
                        }
                    }
                }

                binding.appManagerItemRecyclerView.layoutManager = LinearLayoutManager(this@AppManagerActivity)
                binding.appManagerItemRecyclerView.adapter = itemAppManagerAdapter(appList, this@AppManagerActivity, this@AppManagerActivity)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun fetchSize() {
        Log.d("AppManagerActivity", "Fetching installed app names...")
        binding.progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val apps = withContext(Dispatchers.IO) {
                    packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
                }
                appList = mutableListOf()

                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                withContext(Dispatchers.IO) {
                    apps.forEach { app ->
                        val appName = app.loadLabel(packageManager).toString()
                        val appIcon = packageManager.getApplicationIcon(app)
                        val packageName = app.packageName

                        val isSystemApp = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                        if (!isSystemApp) {
                            val packageInfo = packageManager.getPackageInfo(packageName, 0)
                            val appInstall = sdf.format(Date(packageInfo.firstInstallTime))

                            val appDir = app.sourceDir
                            val appFile = File(appDir)
                            val appSize = (appFile.length() / (1024 * 1024)).toString() + " MB"

                            appList.add(
                                itemAppManagerDataClass(
                                    appName,
                                    appIcon,
                                    packageName,
                                    View.VISIBLE,
                                    appSize,
                                    View.VISIBLE,
                                    appInstall,
                                    View.VISIBLE
                                )
                            )
                        }
                    }
                }

                val sortedAppList =
                    appList.sortedByDescending { it.appSize?.replace(" MB", "")?.toIntOrNull() }

                binding.appManagerItemRecyclerView.layoutManager =
                    LinearLayoutManager(this@AppManagerActivity)
                binding.appManagerItemRecyclerView.adapter = itemAppManagerAdapter(
                    sortedAppList,
                    this@AppManagerActivity,
                    this@AppManagerActivity
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    override fun showDetailsClickListner(item:itemAppManagerDataClass
    ) {        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialogStyle)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.app_manager_detail_layout, null)
        builder.setView(dialogView)


        val appName = dialogView.findViewById<TextView>(R.id.appNameTextView)
        val packageName = dialogView.findViewById<TextView>(R.id.packageNameTextView)
        val icon = dialogView.findViewById<ImageView>(R.id.iconImage)
        val version = dialogView.findViewById<TextView>(R.id.versionTextView)
        val size = dialogView.findViewById<TextView>(R.id.SizeTextView)
        val Installation = dialogView.findViewById<TextView>(R.id.InstallationTextView)
        val InstalledBy = dialogView.findViewById<TextView>(R.id.InstalledByTextView)
        val uninstall=dialogView.findViewById<TextView>(R.id.uninstall)
        val appInfo=dialogView.findViewById<TextView>(R.id.details)
        val share=dialogView.findViewById<TextView>(R.id.share)
        val backup=dialogView.findViewById<TextView>(R.id.backup)

        appName.text=item.appName
        packageName.text=item.packageName
        icon.setImageDrawable(item.appImage)
        size.text=item.appSize
        Installation.text=item.appInstall

        // Mapping of installer package names to friendly names
        val installerFriendlyNames = mapOf(
            "com.android.vending" to "Google Play Store",
            "com.amazon.venezia" to "Amazon Appstore",
            "com.huawei.appmarket" to "Huawei AppGallery",
            "com.sec.android.app.samsungapps" to "Samsung Galaxy Store",
            // Add more mappings as needed
        )

        try {
            val packageInfo = packageManager.getPackageInfo(item.packageName!!, 0)
            val appVersion = packageInfo.versionName
            version.text = "$appVersion"

            val appInstallerPackage = packageManager.getInstallerPackageName(item.packageName!!)
            val appInstallerName = installerFriendlyNames[appInstallerPackage] ?: appInstallerPackage
            InstalledBy.text = appInstallerName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            version.text = "Version: N/A"
            InstalledBy.text = "Installed By: N/A"
        }

        uninstall.setOnClickListener{
            uninstallApp(item.packageName)
        }
        appInfo.setOnClickListener{
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${item.packageName}")
            startActivity(intent)
        }
        share.setOnClickListener{
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            val playStoreLink = "https://play.google.com/store/apps/details?id=${item.packageName}"
            val shareMessage = """
            Check out this app:
            App Name: ${item.appName}
            Play Store Link: $playStoreLink
        """.trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Share app details via"))
        }
        backup.setOnClickListener{
            backupDialog(item)
        }

        val alertDialog = builder.create()
        alertDialog.show()

    }
    private fun backupDialog(item:itemAppManagerDataClass){
        AlertDialog.Builder(this)
            .setTitle("Backup")
            .setMessage("Selected app will be backed up.")
            .setPositiveButton("BACKUP") { _, _ ->
                backupApp(item.packageName, item.appName)
            }
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun backupApp(packageName: String?, appName: String?) {
        if (packageName == null || packageName.isEmpty()) return

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val appInfo = withContext(Dispatchers.IO) {
                    packageManager.getApplicationInfo(packageName, 0)
                }
                val srcFile = File(appInfo.sourceDir)
                val backupDir: File

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backupDir = File(getExternalFilesDir(null), "AppBackups")
                } else {
                    backupDir = File(Environment.getExternalStorageDirectory(), "AppBackups")
                }

                withContext(Dispatchers.IO) {
                    if (!backupDir.exists()) {
                        backupDir.mkdirs()
                    }
                }

                val destFile = File(backupDir, "$appName.apk")

                withContext(Dispatchers.IO) {
                    srcFile.copyTo(destFile, overwrite = true)
                }

                Toast.makeText(this@AppManagerActivity, "Backup successful: ${destFile.absolutePath}", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@AppManagerActivity, "Backup failed", Toast.LENGTH_LONG).show()
            }
        }
    }


}
