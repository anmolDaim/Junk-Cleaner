package com.mobcleaner.mcapp.Activity

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
import android.webkit.MimeTypeMap
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobcleaner.mcapp.Adapter.AppManagerAdapter
import com.mobcleaner.mcapp.Adapter.bigFileCleanerAdapter
import com.mobcleaner.mcapp.DataClass.appManagerDataClass
import com.mobcleaner.mcapp.DataClass.bigFileCleanerDataClass
import com.mobcleaner.mcapp.R
import com.mobcleaner.mcapp.databinding.ActivityFileBigBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.CoroutineContext

class FileBigActivity : AppCompatActivity()
    , bigFileCleanerAdapter.OnCheckboxChangeListener
    , bigFileCleanerAdapter.OnItemClickListener
    ,CoroutineScope {
    private lateinit var binding: ActivityFileBigBinding
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private val PERMISSION_REQUEST_CODE = 101
    private val SPECIAL_PERMISSION_REQUEST_CODE = 102
    private lateinit var CategoryAdapter: AppManagerAdapter
    private lateinit var fileList: MutableList<bigFileCleanerDataClass>
    private lateinit var adapter: bigFileCleanerAdapter

    // SharedPreferences to store the permissions state
    private val PREFS_NAME = "AppManagerPrefs"
    private val PERMISSIONS_GRANTED = "permissions_granted"

    override fun onDestroy() {
        super.onDestroy()
        job.cancel() // Cancel all coroutines when the activity is destroyed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // enableEdgeToEdge()
        binding=ActivityFileBigBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }


        binding.animationView.visibility = View.VISIBLE

        binding.backBtn.setOnClickListener {
            super.onBackPressed()
        }
        // Check for permissions on activity start
        if (checkPermission()) {
            launch {
                displayAllFiles()
            }
        } else {
            // Request permissions
            showPermissionExplanationDialog()
        }



        val catArr = ArrayList<appManagerDataClass>()
        catArr.add(appManagerDataClass("All Types"))
        catArr.add(appManagerDataClass("Images"))
        catArr.add(appManagerDataClass("Video"))
        catArr.add(appManagerDataClass("Audio"))
        catArr.add(appManagerDataClass("Document"))
        catArr.add(appManagerDataClass("Archives"))
        catArr.add(appManagerDataClass("Apk"))

        val linearLayout = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.appManagerCategoryRecyclerView.layoutManager = linearLayout
        CategoryAdapter = AppManagerAdapter(this, catArr) { categoryName ->
            when (categoryName) {
                "All Types" -> displayAllFiles()
                "Images" -> mediaImages()
                "Video" -> mediaVideo()
                "Audio" -> mediaAudio()
                "Document" -> mediaDocument()
                "Archives" -> mediaArchives()
                "Apk" -> mediaApk()
            }
        }

        binding.appManagerCategoryRecyclerView.adapter = CategoryAdapter
    }



    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("This app request permissions to access all files ,images, videos, audio, doc, archive, apk files of your device please grant permission.")
            .setPositiveButton("Allow") { _, _ ->
                requestPermission()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                val intent = Intent(this, MainActivity()::class.java)
                startActivity(intent)
            }
            .create()
            .show()
    }

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

    override fun onCheckboxChanged() {
        val anyChecked = fileList.any { it.isSelected }
        if (anyChecked) {
            binding.deleteButton.setBackgroundResource(R.drawable.optimize_btn) // Replace with your drawable resource for selected state

        } else {
            binding.deleteButton.setBackgroundResource(R.drawable.remove_btn) // Replace with your drawable resource for unselected state
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

    override fun onItemClick(item: bigFileCleanerDataClass) {
        binding.progressBar.visibility = View.VISIBLE
        launch {
            withContext(Dispatchers.Main) {
                showItemDetailsDialog(item)
                binding.progressBar.visibility = View.GONE
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
    private fun deleteSelectedItems() {
        // Iterate through the list of items and delete the selected ones
        launch {
            withContext(Dispatchers.IO) {
                val iterator = fileList.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    if (item.isSelected) {
                        val file = File(item.filePath)
                        if (deleteFile(file)) {
                            iterator.remove()
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@FileBigActivity,
                                    "Failed to delete ${item.fileName}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
            adapter.notifyDataSetChanged()
            withContext(Dispatchers.Main) {
                binding.deleteButton.setBackgroundResource(R.drawable.grey_optimize_btn) // Change the background of the button
            }
        }
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
    private fun displayAllFiles() {
        launch {
            // Initialize fileList as an empty mutable list
            fileList = mutableListOf()

//            // Show progress bar before starting the file loading
//            withContext(Dispatchers.Main) {
//                binding.animationView.visibility = View.VISIBLE
//            }

            // Get the root directory
            val root = Environment.getExternalStorageDirectory()

            // Fetch all files from the root directory
            val files = withContext(Dispatchers.IO) {
                getAllFiles(root)
            }

            // Update the UI with the fetched files
            withContext(Dispatchers.Main) {
                if (files.isEmpty()) {
                    Toast.makeText(this@FileBigActivity, "No files found", Toast.LENGTH_SHORT).show()
                    binding.animationView.visibility = View.GONE
                } else {
                    fileList.addAll(files)
                    binding.animationView.visibility = View.GONE
                   // binding.progressBar.visibility = View.GONE
                    binding.bigFileCleanerRecyclerView.visibility = View.VISIBLE
                    adapter = bigFileCleanerAdapter(fileList, this@FileBigActivity, this@FileBigActivity)
                    binding.bigFileCleanerRecyclerView.adapter = adapter
                    binding.bigFileCleanerRecyclerView.layoutManager = LinearLayoutManager(this@FileBigActivity)
                }
            }
        }
    }


    private fun getAllFiles(dir: File): ArrayList<bigFileCleanerDataClass> {
        val fileList = ArrayList<bigFileCleanerDataClass>()
        val listFile = dir.listFiles()
        if (listFile!= null && listFile.isNotEmpty()) {
            for (file in listFile) {
                if (file.isDirectory) {
                    fileList.addAll(getAllFiles(file))
                } else {
                    val fileSize = formatFileSize(file.length())
                    val fileData = bigFileCleanerDataClass(file.name, fileSize, file.absolutePath)
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

    private fun mediaImages() {
        launch {
            // Initialize fileList as an empty mutable list
            fileList = mutableListOf()

            // Show progress bar before starting the file loading
//            withContext(Dispatchers.Main) {
//                binding.progressBar.visibility = View.VISIBLE
//            }

            // Get the root directory
            val root = Environment.getExternalStorageDirectory()

            // Fetch image files from the root directory
            val files = withContext(Dispatchers.IO) {
                getMediaFiles(root, "image")
            }

            // Update the UI with the fetched files
            withContext(Dispatchers.Main) {
                if (files.isEmpty()) {
                    Toast.makeText(this@FileBigActivity, "No image files found", Toast.LENGTH_SHORT).show()
                    //binding.progressBar.visibility = View.GONE
                } else {
                    fileList.addAll(files)
                    //binding.progressBar.visibility = View.GONE
                    binding.bigFileCleanerRecyclerView.visibility = View.VISIBLE
                    adapter = bigFileCleanerAdapter(fileList, this@FileBigActivity, this@FileBigActivity)
                    binding.bigFileCleanerRecyclerView.adapter = adapter
                    binding.bigFileCleanerRecyclerView.layoutManager = LinearLayoutManager(this@FileBigActivity)
                }
            }
        }
    }
    private fun getMediaFiles(dir: File, mediaType: String): ArrayList<bigFileCleanerDataClass> {
        val fileList = ArrayList<bigFileCleanerDataClass>()
        val listFile = dir.listFiles()
        if (listFile != null && listFile.isNotEmpty()) {
            for (file in listFile) {
                if (file.isDirectory) {
                    fileList.addAll(getMediaFiles(file, mediaType))
                } else {
                    val mimeType = getMimeType(file.absolutePath)
                    if (mimeType?.startsWith(mediaType) == true) {
                        val fileSize = formatFileSize(file.length())
                        val fileData = bigFileCleanerDataClass(file.name, fileSize, file.absolutePath)
                        fileList.add(fileData)
                    }
                }
            }
        }
        return fileList
    }

    private fun getMimeType(url: String): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(url) ?: return null
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }
    private fun mediaAudio() {
        launch {
            // Initialize fileList as an empty mutable list
            fileList = mutableListOf()

            // Show progress bar before starting the file loading
//            withContext(Dispatchers.Main) {
//                binding.progressBar.visibility = View.VISIBLE
//            }

            // Get the root directory
            val root = Environment.getExternalStorageDirectory()

            // Fetch audio files from the root directory
            val files = withContext(Dispatchers.IO) {
                getMediaFiles(root, "audio")
            }

            // Update the UI with the fetched files
            withContext(Dispatchers.Main) {
                if (files.isEmpty()) {
                    Toast.makeText(this@FileBigActivity, "No audio files found", Toast.LENGTH_SHORT).show()
                    //binding.progressBar.visibility = View.GONE
                } else {
                    fileList.addAll(files)
                   // binding.progressBar.visibility = View.GONE
                    binding.bigFileCleanerRecyclerView.visibility = View.VISIBLE
                    adapter = bigFileCleanerAdapter(fileList, this@FileBigActivity, this@FileBigActivity)
                    binding.bigFileCleanerRecyclerView.adapter = adapter
                    binding.bigFileCleanerRecyclerView.layoutManager = LinearLayoutManager(this@FileBigActivity)
                }
            }
        }
    }
    private fun mediaVideo() {
        launch {
            // Initialize fileList as an empty mutable list
            fileList = mutableListOf()

            // Show progress bar before starting the file loading
//            withContext(Dispatchers.Main) {
//                binding.progressBar.visibility = View.VISIBLE
//            }

            // Get the root directory
            val root = Environment.getExternalStorageDirectory()

            // Fetch video files from the root directory
            val files = withContext(Dispatchers.IO) {
                getMediaFiles(root, "video")
            }

            // Update the UI with the fetched files
            withContext(Dispatchers.Main) {
                if (files.isEmpty()) {
                    Toast.makeText(this@FileBigActivity, "No video files found", Toast.LENGTH_SHORT).show()
                    //binding.progressBar.visibility = View.GONE
                } else {
                    fileList.addAll(files)
                    //binding.progressBar.visibility = View.GONE
                    binding.bigFileCleanerRecyclerView.visibility = View.VISIBLE
                    adapter = bigFileCleanerAdapter(fileList, this@FileBigActivity, this@FileBigActivity)
                    binding.bigFileCleanerRecyclerView.adapter = adapter
                    binding.bigFileCleanerRecyclerView.layoutManager = LinearLayoutManager(this@FileBigActivity)
                }
            }
        }
    }

    // Function to display document files
    private fun mediaDocument() {
        launch {
            // Initialize fileList as an empty mutable list
            fileList = mutableListOf()

            // Show progress bar before starting the file loading
//            withContext(Dispatchers.Main) {
//                binding.progressBar.visibility = View.VISIBLE
//            }

            // Get the root directory
            val root = Environment.getExternalStorageDirectory()

            // Fetch document files from the root directory
            val files = withContext(Dispatchers.IO) {
                getDocumentFiles(root)
            }

            // Update the UI with the fetched files
            withContext(Dispatchers.Main) {
                if (files.isEmpty()) {
                    Toast.makeText(this@FileBigActivity, "No document files found", Toast.LENGTH_SHORT).show()
                    //binding.progressBar.visibility = View.GONE
                } else {
                    fileList.addAll(files)
                   // binding.progressBar.visibility = View.GONE
                    binding.bigFileCleanerRecyclerView.visibility = View.VISIBLE
                    adapter = bigFileCleanerAdapter(fileList, this@FileBigActivity, this@FileBigActivity)
                    binding.bigFileCleanerRecyclerView.adapter = adapter
                    binding.bigFileCleanerRecyclerView.layoutManager = LinearLayoutManager(this@FileBigActivity)
                }
            }
        }
    }

    // Function to fetch document files
    private fun getDocumentFiles(dir: File): ArrayList<bigFileCleanerDataClass> {
        val fileList = ArrayList<bigFileCleanerDataClass>()
        val listFile = dir.listFiles()
        if (listFile != null && listFile.isNotEmpty()) {
            for (file in listFile) {
                if (file.isDirectory) {
                    fileList.addAll(getDocumentFiles(file))
                } else {
                    val mimeType = getMimeType(file.absolutePath)
                    if (mimeType?.startsWith("application") == true || mimeType?.startsWith("text") == true) {
                        val fileSize = formatFileSize(file.length())
                        val fileData = bigFileCleanerDataClass(file.name, fileSize, file.absolutePath)
                        fileList.add(fileData)
                    }
                }
            }
        }
        return fileList
    }
    // Call this method when the "Archives" category is selected
    private fun mediaArchives() {
        launch {
            fileList = mutableListOf()
//            withContext(Dispatchers.Main) {
//                binding.progressBar.visibility = View.VISIBLE
//            }
            val root = Environment.getExternalStorageDirectory()
            val files = withContext(Dispatchers.IO) {
                getArchiveFiles(root)
            }
            withContext(Dispatchers.Main) {
                if (files.isEmpty()) {
                    Toast.makeText(this@FileBigActivity, "No archive files found", Toast.LENGTH_SHORT).show()
                    //binding.progressBar.visibility = View.GONE
                } else {
                    fileList.addAll(files)
                    //binding.progressBar.visibility = View.GONE
                    binding.bigFileCleanerRecyclerView.visibility = View.VISIBLE
                    adapter = bigFileCleanerAdapter(fileList, this@FileBigActivity, this@FileBigActivity)
                    binding.bigFileCleanerRecyclerView.adapter = adapter
                    binding.bigFileCleanerRecyclerView.layoutManager = LinearLayoutManager(this@FileBigActivity)
                }
            }
        }
    }

    private fun getArchiveFiles(dir: File): ArrayList<bigFileCleanerDataClass> {
        val fileList = ArrayList<bigFileCleanerDataClass>()
        val listFile = dir.listFiles()
        if (listFile != null && listFile.isNotEmpty()) {
            for (file in listFile) {
                if (file.isDirectory) {
                    fileList.addAll(getArchiveFiles(file))
                } else {
                    val mimeType = getMimeType(file.absolutePath)
                    if (mimeType == "application/zip" || mimeType == "application/x-rar-compressed" ||
                        mimeType == "application/x-7z-compressed" || mimeType == "application/gzip") {
                        val fileSize = formatFileSize(file.length())
                        val fileData = bigFileCleanerDataClass(file.name, fileSize, file.absolutePath)
                        fileList.add(fileData)
                    }
                }
            }
        }
        return fileList
    }

    // Call this method when the "Apk" category is selected
    private fun mediaApk() {
        launch {
            fileList = mutableListOf()
//            withContext(Dispatchers.Main) {
//                binding.progressBar.visibility = View.VISIBLE
//            }
            val root = Environment.getExternalStorageDirectory()
            val files = withContext(Dispatchers.IO) {
                getApkFiles(root)
            }
            withContext(Dispatchers.Main) {
                if (files.isEmpty()) {
                    Toast.makeText(this@FileBigActivity, "No APK files found", Toast.LENGTH_SHORT).show()
                    //binding.progressBar.visibility = View.GONE
                } else {
                    fileList.addAll(files)
                    //binding.progressBar.visibility = View.GONE
                    binding.bigFileCleanerRecyclerView.visibility = View.VISIBLE
                    adapter = bigFileCleanerAdapter(fileList, this@FileBigActivity, this@FileBigActivity)
                    binding.bigFileCleanerRecyclerView.adapter = adapter
                    binding.bigFileCleanerRecyclerView.layoutManager = LinearLayoutManager(this@FileBigActivity)
                }
            }
        }
    }

    private fun getApkFiles(dir: File): ArrayList<bigFileCleanerDataClass> {
        val fileList = ArrayList<bigFileCleanerDataClass>()
        val listFile = dir.listFiles()
        if (listFile != null && listFile.isNotEmpty()) {
            for (file in listFile) {
                if (file.isDirectory) {
                    fileList.addAll(getApkFiles(file))
                } else {
                    val mimeType = getMimeType(file.absolutePath)
                    if (mimeType == "application/vnd.android.package-archive") {
                        val fileSize = formatFileSize(file.length())
                        val fileData = bigFileCleanerDataClass(file.name, fileSize, file.absolutePath)
                        fileList.add(fileData)
                    }
                }
            }
        }
        return fileList
    }


}