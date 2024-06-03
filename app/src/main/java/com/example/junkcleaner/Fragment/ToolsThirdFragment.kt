package com.example.junkcleaner.Fragment

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.junkcleaner.Adapter.JunkFilesAdapter
import com.example.junkcleaner.Adapter.bigFileCleanerAdapter
import com.example.junkcleaner.DataClass.bigFileCleanerDataClass
import com.example.junkcleaner.DataClass.junkFiles
import com.example.junkcleaner.R
import com.example.junkcleaner.databinding.FragmentToolsFirstBinding
import com.example.junkcleaner.databinding.FragmentToolsSecondBinding
import com.example.junkcleaner.databinding.FragmentToolsThirdBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class ToolsThirdFragment : Fragment() {

    private var _binding: FragmentToolsThirdBinding? = null
    private val binding get() = _binding!!
    private val PERMISSION_REQUEST_CODE = 101
    private val SPECIAL_PERMISSION_REQUEST_CODE = 102
    private lateinit var junkList: ArrayList<junkFiles>
    private lateinit var adapter: JunkFilesAdapter

    // SharedPreferences to store the permissions state
    private val PREFS_NAME = "AppManagerPrefs"
    private val PERMISSIONS_GRANTED = "permissions_granted"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentToolsThirdBinding.inflate(inflater, container, false)

        return  binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView and its adapter
        binding.junkFilesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        junkList = ArrayList()
        adapter = JunkFilesAdapter(junkList)
        binding.junkFilesRecyclerView.adapter = adapter

        // Check for permissions on activity start
        if (checkPermission()) {
            // Permissions are already granted
            displayAllFiles()
            binding.junkCleanButton.setOnClickListener {
               // cleanJunkFiles()
            }
        } else {
            // Request permissions
            showPermissionExplanationDialog()
        }

    }
    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(requireContext())
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

    private fun checkPermission(): Boolean {
        val sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val permissionsGranted = sharedPreferences.getBoolean(PERMISSIONS_GRANTED, false)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager() && permissionsGranted
        } else {
            val readExternalStoragePermission = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            val writeExternalStoragePermission = ContextCompat.checkSelfPermission(
                requireContext(),
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
                intent.data = Uri.parse(String.format("package:%s", requireContext().packageName))
                startActivityForResult(intent, SPECIAL_PERMISSION_REQUEST_CODE)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, SPECIAL_PERMISSION_REQUEST_CODE)
            }
        } else {
            ActivityCompat.requestPermissions(
                requireContext() as Activity,
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
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun savePermissionState(granted: Boolean) {
        val sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(PERMISSIONS_GRANTED, granted)
        editor.apply()
    }
    private fun displayAllFiles() {

        val root = Environment.getExternalStorageDirectory()
        val files = getAllJunkFiles(root)
        val totalSize = calculateTotalSize(files)
        binding.totalSizeTextView.text = "Total Size: $totalSize"
        junkList.clear()
        junkList.addAll(files)
        adapter.notifyDataSetChanged()
    }
    private fun calculateTotalSize(junkFiles: List<junkFiles>): String {
        var totalSize = 0.0
        for (file in junkFiles) {
            totalSize += file.size ?: 0.0
        }
        return formatSize(totalSize)
    }

    private fun formatSize(size: Double): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> String.format("%.2f B", size)
        }
    }
    private fun getAllJunkFiles(dir: File): List<junkFiles> {
        val junkFiles = mutableListOf<junkFiles>()

        // Iterate through all files and directories
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                // Recursively explore subdirectories
                junkFiles.addAll(getAllJunkFiles(file))
            } else {
                // Check if the file is a junk file
                if (isJunkFile(file)) {
                    // Add junk file to the list
                    junkFiles.add(junkFiles(file.name,file.length().toDouble(),null))
                }
            }
        }

        return junkFiles
    }

    private fun isJunkFile(file: File): Boolean {
        // Define your criteria for identifying junk files here
        val fileExtension = file.extension.toLowerCase()
        return (fileExtension == "tmp" || fileExtension == "bak" || fileExtension == "temp")
    }

//    private fun getAllFiles(dir: File): Collection<junkFiles> {
//        val fileList = ArrayList<junkFiles>()
//        val listFile = dir.listFiles()
//        if (listFile != null && listFile.isNotEmpty()) {
//            for (file in listFile) {
//                if (file.isDirectory) {
//                    fileList.addAll(getAllFiles(file))
//                } else {
//                    //val fileDate = getFileDate(file)
//                    val fileData = junkFiles(file.name)
//                    fileList.add(fileData)
//                }
//            }
//        }
//        return fileList
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}