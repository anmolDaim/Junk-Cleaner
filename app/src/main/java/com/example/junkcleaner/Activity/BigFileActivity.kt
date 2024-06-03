package com.example.junkcleaner.Activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.junkcleaner.Adapter.bigFileCleanerAdapter
import com.example.junkcleaner.DataClass.bigFileCleanerDataClass
import com.example.junkcleaner.R
import com.example.junkcleaner.databinding.ActivityBigFileBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BigFileActivity : AppCompatActivity()
    , bigFileCleanerAdapter.OnCheckboxChangeListener
    , bigFileCleanerAdapter.OnItemClickListener
    {
    private lateinit var binding: ActivityBigFileBinding
    private val PERMISSION_REQUEST_CODE = 101
    private val SPECIAL_PERMISSION_REQUEST_CODE = 102
    private val fileList = ArrayList<bigFileCleanerDataClass>()
    private lateinit var adapter: bigFileCleanerAdapter

        // SharedPreferences to store the permissions state
        private val PREFS_NAME = "AppManagerPrefs"
        private val PERMISSIONS_GRANTED = "permissions_granted"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBigFileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.progressBar.visibility = View.VISIBLE

binding.backBtn.setOnClickListener{
    super.onBackPressed()
}
        // Initialize RecyclerView and its adapter
        binding.bigFileCleanerRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = bigFileCleanerAdapter(fileList,this,this)
        binding.bigFileCleanerRecyclerView.adapter = adapter


        // Initialize and set up the spinners
        setupSpinners()

        val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Check for permissions on activity start
        if (checkPermission()) {
            // Permissions are already granted
            displayAllFiles()
        } else {
            // Request permissions
            showPermissionExplanationDialog()
        }


    }
        private fun showPermissionExplanationDialog() {
            AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("This app needs permissions to access app details like size, installation date, and last used time. Please grant these permissions to proceed.")
                .setPositiveButton("Allow") { _, _ ->
                    requestPermission()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .create()
                .show()
        }

    private fun setupSpinners() {
        // Create ArrayAdapter using the custom spinner item layout
        val adapter1 = ArrayAdapter.createFromResource(
            this,
            R.array.dropdown_items1,
            R.layout.custom_spinner_layout
        )
        val adapter2 = ArrayAdapter.createFromResource(
            this,
            R.array.dropdown_items2,
            R.layout.custom_spinner_layout
        )
        val adapter3 = ArrayAdapter.createFromResource(
            this,
            R.array.dropdown_items3,
            R.layout.custom_spinner_layout
        )

        // Specify the layout to use when the list of choices appears
        adapter1.setDropDownViewResource(R.layout.custom_spinner_layout)
        adapter2.setDropDownViewResource(R.layout.custom_spinner_layout)
        adapter3.setDropDownViewResource(R.layout.custom_spinner_layout)

        // Apply the adapters to the spinners
        binding.spinner1.adapter = adapter1
        binding.spinner2.adapter = adapter2
        binding.spinner3.adapter = adapter3

        // Set listeners for the spinners
        binding.spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                when(selectedItem){
                    "All Types" -> displayAllFiles()
                    "Images" -> mediaImages()
                    "Video" -> mediaVideo()
                    "Audio" -> mediaAudio()
                    "Document" -> mediaDocument()
                    "Archives" -> mediaArchives()
                    "Apk" -> mediaApk()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        binding.spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

                when(selectedItem){
                    "10MB"-> tenMegaByte()
                    "50MB"->fiftyMegaByte()
                    "100MB"->hundredMegaByte()
                    "500MB"->fiveHundredMegaByte()
                    "1GB"->oneGigaByte()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        binding.spinner3.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()

                when(selectedItem){
                    "1 week" -> oneWeek()
                    "1 month"-> oneMonth()
                    "3 month" -> threeMonth()
                    "6 month" -> sixMonth()
                    "1 year" -> oneYear()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
    }
        private fun oneWeek() {
            val currentTime = System.currentTimeMillis()
            Log.d("currentTime", "File path: ${currentTime}")

            val oneWeekAgo = currentTime - (7 * 24 * 60 * 60 * 1000L)
            filterFilesByDate(oneWeekAgo)
        }
        private fun oneMonth() {
            val currentTime = System.currentTimeMillis()
            val threeMonthAgo = currentTime - (30 * 24 * 60 * 60 * 1000L)
            filterFilesByDate(threeMonthAgo)
        }

        private fun threeMonth() {
            val currentTime = System.currentTimeMillis()
            val threeMonthAgo = currentTime - (90 * 24 * 60 * 60 * 1000L)
            filterFilesByDate(threeMonthAgo)
        }

        private fun sixMonth() {
            val currentTime = System.currentTimeMillis()
            val sixMonthAgo = currentTime - (180 * 24 * 60 * 60 * 1000L)
            filterFilesByDate(sixMonthAgo)
        }

        private fun oneYear() {
            val currentTime = System.currentTimeMillis()
            val oneYearAgo = currentTime - (365 * 24 * 60 * 60 * 1000L)
            filterFilesByDate(oneYearAgo)
        }

        private fun filterFilesByDate(time: Long) {
            val filteredFiles = fileList.filter { file ->
                val fileLastModified = File(file.filePath).lastModified()
                fileLastModified < time
            }
            fileList.clear()
            fileList.addAll(filteredFiles)
            adapter.notifyDataSetChanged()
        }

        private fun oneGigaByte() {
            // Filter files based on size
            val filteredFiles = fileList.filter { file ->
                val sizeInBytes = File(file.filePath).length()
                val sizeInMB = sizeInBytes / (1024.0 * 1024.0)
                sizeInMB >= 1024
            }

            // Clear the existing list
            fileList.clear()

            // Add filtered files to the list
            fileList.addAll(filteredFiles)

            // Notify the adapter that the data set has changed
            adapter.notifyDataSetChanged()
        }

        private fun fiveHundredMegaByte() {
            // Filter files based on size
            val filteredFiles = fileList.filter { file ->
                val sizeInBytes = File(file.filePath).length()
                val sizeInMB = sizeInBytes / (1024.0 * 1024.0)
                sizeInMB >= 500
            }

            // Clear the existing list
            fileList.clear()

            // Add filtered files to the list
            fileList.addAll(filteredFiles)

            // Notify the adapter that the data set has changed
            adapter.notifyDataSetChanged()
        }

        private fun hundredMegaByte() {
            // Filter files based on size
            val filteredFiles = fileList.filter { file ->
                val sizeInBytes = File(file.filePath).length()
                val sizeInMB = sizeInBytes / (1024.0 * 1024.0)
                sizeInMB >= 100
            }

            // Clear the existing list
            fileList.clear()

            // Add filtered files to the list
            fileList.addAll(filteredFiles)

            // Notify the adapter that the data set has changed
            adapter.notifyDataSetChanged()
        }

        private fun fiftyMegaByte() {
            // Filter files based on size
            val filteredFiles = fileList.filter { file ->
                val sizeInBytes = File(file.filePath).length()
                val sizeInMB = sizeInBytes / (1024.0 * 1024.0)
                sizeInMB >= 50
            }

            // Clear the existing list
            fileList.clear()

            // Add filtered files to the list
            fileList.addAll(filteredFiles)

            // Notify the adapter that the data set has changed
            adapter.notifyDataSetChanged()
        }

        private fun tenMegaByte() {
            // Filter files based on size
            val filteredFiles = fileList.filter { file ->
                val sizeInBytes = File(file.filePath).length()
                val sizeInMB = sizeInBytes / (1024.0 * 1024.0)
                sizeInMB >= 10
            }

            // Clear the existing list
            fileList.clear()

            // Add filtered files to the list
            fileList.addAll(filteredFiles)

            // Notify the adapter that the data set has changed
            adapter.notifyDataSetChanged()
        }

        private fun mediaApk() {
            binding.progressBar.visibility = View.VISIBLE

            // Filter only APK files
            val apkFiles = fileList.filter {
                it.fileName.endsWith(".apk", ignoreCase = true)
            }

            // Clear the existing list
            fileList.clear()

            // Add APK files to the list
            fileList.addAll(apkFiles)

            // Notify the adapter that the data set has changed
            adapter.notifyDataSetChanged()

            binding.progressBar.visibility = View.GONE
        }

        private fun mediaArchives() {
            binding.progressBar.visibility = View.VISIBLE

            // Define the archive file extensions
            val archiveExtensions = listOf(".zip", ".rar", ".tar")

            // Filter only archive files
            val archiveFiles = fileList.filter { file ->
                archiveExtensions.any { ext -> file.fileName.endsWith(ext, ignoreCase = true) }
            }

            // Clear the existing list
            fileList.clear()

            // Add archive files to the list
            fileList.addAll(archiveFiles)

            // Notify the adapter that the data set has changed
            adapter.notifyDataSetChanged()

            binding.progressBar.visibility = View.GONE
        }

        private fun mediaDocument() {

            binding.progressBar.visibility = View.VISIBLE

            // Filter only document files
            val documentFiles = fileList.filter {
                it.fileName.endsWith(".pdf", ignoreCase = true) ||
                        it.fileName.endsWith(".doc", ignoreCase = true) ||
                        it.fileName.endsWith(".docx", ignoreCase = true) ||
                        it.fileName.endsWith(".txt", ignoreCase = true) ||
                        it.fileName.endsWith(".xls", ignoreCase = true) ||
                        it.fileName.endsWith(".xlsx", ignoreCase = true) ||
                        it.fileName.endsWith(".ppt", ignoreCase = true) ||
                        it.fileName.endsWith(".pptx", ignoreCase = true) ||
                        it.fileName.endsWith(".csv", ignoreCase = true) ||
                        it.fileName.endsWith(".rtf", ignoreCase = true)
            }

            // Clear the existing list
            fileList.clear()

            // Add document files to the list
            fileList.addAll(documentFiles)

            // Notify the adapter that the data set has changed
            adapter.notifyDataSetChanged()

            binding.progressBar.visibility = View.GONE

        }

        private fun mediaAudio() {
            binding.progressBar.visibility = View.VISIBLE
// Filter only audio files
            val audioFiles = fileList.filter {
                it.fileName.endsWith(".mp3", ignoreCase = true) ||
                        it.fileName.endsWith(".wav", ignoreCase = true) ||
                        it.fileName.endsWith(".aac", ignoreCase = true) ||
                        it.fileName.endsWith(".flac", ignoreCase = true) ||
                        it.fileName.endsWith(".m4a", ignoreCase = true) ||
                        it.fileName.endsWith(".ogg", ignoreCase = true) ||
                        it.fileName.endsWith(".wma", ignoreCase = true) ||
                        it.fileName.endsWith(".amr", ignoreCase = true)
            }

            // Clear the existing list
            fileList.clear()

            // Add audio files to the list
            fileList.addAll(audioFiles)

            // Notify the adapter that the data set has changed
            adapter.notifyDataSetChanged()

            binding.progressBar.visibility = View.GONE
        }

        private fun mediaVideo() {
            binding.progressBar.visibility = View.VISIBLE
// Filter only video files
            val videoFiles = fileList.filter {
                it.fileName.endsWith(".mp4", ignoreCase = true) ||
                        it.fileName.endsWith(".mkv", ignoreCase = true) ||
                        it.fileName.endsWith(".avi", ignoreCase = true) ||
                        it.fileName.endsWith(".mov", ignoreCase = true) ||
                        it.fileName.endsWith(".flv", ignoreCase = true) ||
                        it.fileName.endsWith(".wmv", ignoreCase = true) ||
                        it.fileName.endsWith(".3gp", ignoreCase = true) ||
                        it.fileName.endsWith(".webm", ignoreCase = true)
            }

            // Clear the existing list
            fileList.clear()

            // Add video files to the list
            fileList.addAll(videoFiles)

            // Notify the adapter that the data set has changed
            adapter.notifyDataSetChanged()

            binding.progressBar.visibility = View.GONE
        }

        private fun mediaImages() {
            binding.progressBar.visibility = View.VISIBLE
// Filter only image files
        val imageFiles = fileList.filter { it.fileName.endsWith(".jpg", ignoreCase = true) || it.fileName.endsWith(".jpeg", ignoreCase = true) || it.fileName.endsWith(".png", ignoreCase = true) }

        // Clear the existing list
        fileList.clear()

        // Add image files to the list
        fileList.addAll(imageFiles)

        // Notify the adapter thatnoteno the data set has changed
        adapter.notifyDataSetChanged()

            binding.progressBar.visibility = View.GONE
    }

//    private fun alltype() {
//        // Check permission for reading external storage
//        if (checkPermission()) {
//            // Permission already granted
//            displayAllFiles()
//        } else {
//            // Request permission
//            requestPermission()
//        }
//    }
//
    private fun displayAllFiles() {
    binding.progressBar.visibility = View.VISIBLE
        val root = Environment.getExternalStorageDirectory()
        val files = getAllFiles(root)
        fileList.clear()
        fileList.addAll(files)
        adapter.notifyDataSetChanged()

    binding.progressBar.visibility = View.GONE
    }

    private fun getAllFiles(dir: File): ArrayList<bigFileCleanerDataClass> {
        val fileList = ArrayList<bigFileCleanerDataClass>()
        val listFile = dir.listFiles()
        if (listFile != null && listFile.isNotEmpty()) {
            for (file in listFile) {
                if (file.isDirectory) {
                    fileList.addAll(getAllFiles(file))
                } else {
                    val fileSize = formatFileSize(file.length())
                    //val fileDate = getFileDate(file)
                    val fileData = bigFileCleanerDataClass(file.name,fileSize
//                        ,fileDate
                        ,file.absolutePath
                    )
                    fileList.add(fileData)
                }
            }
        }
        return fileList
    }

    private fun formatFileSize(sizeInBytes: Long): String {
        val sizeInKB = sizeInBytes / 1024.0
        val sizeInMB = sizeInKB / 1024.0
        val sizeInGB = sizeInMB / 1024.0

        return when {
            sizeInGB >= 1 -> String.format("%.2f GB", sizeInGB)
            sizeInMB >= 1 -> String.format("%.2f MB", sizeInMB)
            sizeInKB >= 1 -> String.format("%.2f KB", sizeInKB)
            else -> String.format("%d Bytes", sizeInBytes)
        }
    }
////
////
private fun checkPermission(): Boolean {
    val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val permissionsGranted = sharedPreferences.getBoolean(PERMISSIONS_GRANTED, false)

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager() && permissionsGranted
    } else {
        val readExternalStoragePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val writeExternalStoragePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        readExternalStoragePermission == PackageManager.PERMISSION_GRANTED &&
                writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED &&
                permissionsGranted
    }
}

        private fun requestPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse(String.format("package:%s", this.packageName))
                    startActivityForResult(intent, SPECIAL_PERMISSION_REQUEST_CODE)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    startActivityForResult(intent, SPECIAL_PERMISSION_REQUEST_CODE)
                }
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ),
                    PERMISSION_REQUEST_CODE
                )
            }
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, save the state in SharedPreferences
                    savePermissionState(true)
                    displayAllFiles()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == SPECIAL_PERMISSION_REQUEST_CODE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        // Permission granted, save the state in SharedPreferences
                        savePermissionState(true)
                        displayAllFiles()
                    } else {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        private fun savePermissionState(granted: Boolean) {
            val sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean(PERMISSIONS_GRANTED, granted)
            editor.apply()
        }

    override fun onCheckboxChanged() {
        val anyChecked = fileList.any { it.isSelected }
        if (anyChecked) {
            binding.deleteButton.setBackgroundResource(R.drawable.optimize_btn) // Replace with your drawable resource for selected state

        } else {
            binding.deleteButton.setBackgroundResource(R.drawable.grey_optimize_btn) // Replace with your drawable resource for unselected state
        }
        binding.deleteButton.setOnClickListener{
// Check if any items are selected for deletion
            val anyChecked = fileList.any { it.isSelected }
            if (anyChecked) {
                showDeleteConfirmationDialog()
            } else {
                Toast.makeText(this, "No items selected for deletion", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showDeleteConfirmationDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Junk Cleaner")
        alertDialogBuilder.setMessage("Selected file cannot be recovered after deleting, Are you sure you want to delete the selected items?")

        alertDialogBuilder.setPositiveButton("Delete") { dialog, _ ->
            deleteSelectedItems()
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
//
    private fun deleteSelectedItems() {
        // Iterate through the list of items and delete the selected ones
        val iterator = fileList.iterator()

        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.isSelected) {
                Log.d("SelectedFilePath", "File path: ${item.filePath}")
                val file = File(item.filePath)
                val deleted = deleteFile(file)
                if (deleted) {
                    // Remove the item from the list if deletion was successful
                    iterator.remove()
                } else {
                    // Handle deletion failure
                    Toast.makeText(this, "Failed to delete ${item.fileName}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // Notify the adapter that the data set has changed
        adapter.notifyDataSetChanged()
    }

    private fun deleteFile(file: File): Boolean {
        return try {
            if (file.isDirectory) {
                file.listFiles()?.forEach { deleteFile(it) }
            }
                val deleted = file.delete()
                Log.d("FileDeletion", "Deleted: ${file.absolutePath} - $deleted")
            deleted
        } catch (e: Exception) {
            Log.e("FileDeletion", "Error deleting file: ${file.absolutePath}", e)

            false
        }
    }
        private fun getFileDate(file: File): String {
            val lastModified = file.lastModified()
            val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
            return sdf.format(Date(lastModified))
        }

        // Implement the required method for OnItemClickListener
        override fun onItemClick(item: bigFileCleanerDataClass) {
            binding.progressBar.visibility=View.VISIBLE
            showItemDetailsDialog(item)
        }

         fun showItemDetailsDialog(item: bigFileCleanerDataClass) {
             binding.progressBar.visibility=View.GONE
            val builder = AlertDialog.Builder(this, R.style.CustomAlertDialogStyle)
            val inflater = LayoutInflater.from(this)
            val dialogView = inflater.inflate(R.layout.file_status_dialog, null)
            builder.setView(dialogView)

            val fileName = dialogView.findViewById<TextView>(R.id.dialogFilename)
            val fileDate = dialogView.findViewById<TextView>(R.id.dialogFileDate)
            val fileSize = dialogView.findViewById<TextView>(R.id.dialogFileSize)
            val filePath = dialogView.findViewById<TextView>(R.id.dialogFilePath)
            val viewButton = dialogView.findViewById<AppCompatButton>(R.id.viewDialog)
            val cancelButton = dialogView.findViewById<AppCompatButton>(R.id.cancelDialog)

            fileName.text = item.fileName
            fileDate.text = getFileDate(File(item.filePath))
            fileSize.text = item.fileSize
            filePath.text = item.filePath

            val dialog = builder.create()

            viewButton.setOnClickListener {
                val file = File(item.filePath)
                val fileUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    FileProvider.getUriForFile(this, "${packageName}.provider", file)
                } else {
                    Uri.fromFile(file)
                }

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = fileUri
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }

                val mimeType = contentResolver.getType(fileUri) ?: "*/*"
                intent.setDataAndType(fileUri, mimeType)

                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "No application found to open this file type", Toast.LENGTH_SHORT).show()
                }
            }

            cancelButton.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

    }