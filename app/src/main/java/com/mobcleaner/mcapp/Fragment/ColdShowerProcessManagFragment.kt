package com.mobcleaner.mcapp.Fragment

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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.mobcleaner.mcapp.Activity.MainActivity
import com.mobcleaner.mcapp.DataClass.junkFiles
import com.mobcleaner.mcapp.R
import com.mobcleaner.mcapp.databinding.FragmentColdShowerProcessManagBinding
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext

class ColdShowerProcessManagFragment : Fragment(), CoroutineScope {
    private lateinit var binding: FragmentColdShowerProcessManagBinding
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private val PERMISSION_REQUEST_CODE = 101
    private val SPECIAL_PERMISSION_REQUEST_CODE = 102
    private val PREFS_NAME = "AppManagerPrefs"
    private val PERMISSIONS_GRANTED = "permissions_granted"

    private var isFragmentVisible = false
    private var cleaningDialog: AlertDialog? = null
    private var dialogShowing = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentColdShowerProcessManagBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Uncomment and set animation if needed
        // binding.SuccessfullLoader.setAnimation(R.raw.animation)
        // binding.SuccessfullLoader.playAnimation()

        binding.animationview.visibility = View.VISIBLE
        binding.coldShowerBtn.setOnClickListener {
            if (checkPermission()) {
                handleJunkFiles()
            } else {
                requestPermission()
            }
        }

        if (checkPermission()) {
            displayAllFiles()
        } else {
            requestPermission()
        }
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
                savePermissionState(true)
                displayAllFiles()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayAllFiles() {
        launch(Dispatchers.IO) {
            val root = Environment.getExternalStorageDirectory()
            val files = getAllJunkFiles(root)
            val totalSize = calculateTotalSize(files)
            withContext(Dispatchers.Main) {
                if (isFragmentVisible) {
                    if (files.isEmpty()) {
                        binding.tvTextSize.text = "No junk files "
                        if (!dialogShowing) {
                            val dialogView = LayoutInflater.from(requireContext())
                                .inflate(R.layout.no_file_dialog, null)

                            val dialog = AlertDialog.Builder(requireContext())
                                .setView(dialogView)
                                .create()
                            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
                            dialogView.findViewById<AppCompatButton>(R.id.noFileBtn)
                                .setOnClickListener {
                                    val intent = Intent(requireContext(), MainActivity::class.java)
                                    startActivity(intent)
                                    dialog.dismiss()
                                }
                            dialog.show()
                        }
                    } else {
                        binding.coldShowerBtn.setBackgroundResource(R.drawable.optimize_btn)
                        binding.tvTextSize.text = totalSize
                    }
                    binding.animationview.visibility = View.GONE
                    binding.searchImg.visibility = View.GONE
                    binding.animationview2.visibility = View.VISIBLE
                }
            }
        }
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
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                junkFiles.addAll(getAllJunkFiles(file))
            } else {
                if (isJunkFile(file)) {
                    junkFiles.add(junkFiles(file.absolutePath, file.length().toDouble(), file.path))
                }
            }
        }
        return junkFiles
    }

    private fun isJunkFile(file: File): Boolean {
        val fileExtension = file.extension.toLowerCase()
        val fileName = file.name.toLowerCase()
        val junkExtensions = setOf("tmp", "bak", "temp", "log", "cache")
        val junkDirectories = setOf("cache", "temp", "logs")

        val isInJunkDirectory = file.parentFile?.name?.toLowerCase() in junkDirectories
        return fileExtension in junkExtensions || isInJunkDirectory || fileName.contains("cache") || fileName.contains("temp")
    }

    private fun handleJunkFiles() {
        launch(Dispatchers.IO) {
            val root = Environment.getExternalStorageDirectory()
            val junkFiles = getAllJunkFiles(root)
            withContext(Dispatchers.Main) {
                if (junkFiles.isEmpty()) {
                    Toast.makeText(requireContext(), "No junk files found", Toast.LENGTH_SHORT).show()
                } else {
                    // Navigate to ToolsThirdFragment
                    val fragmentManager = requireActivity().supportFragmentManager
                    val fragmentTransaction = fragmentManager.beginTransaction()
                    val toolsThirdFragment = ToolsThirdFragment()

                    // Pass junk files to the new fragment
                    val bundle = Bundle()
                    bundle.putSerializable("junkFiles", ArrayList(junkFiles))
                    toolsThirdFragment.arguments = bundle

                    fragmentTransaction.replace(R.id.container, toolsThirdFragment)
                    fragmentTransaction.addToBackStack(null)
                    fragmentTransaction.commit()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPECIAL_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
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

    override fun onResume() {
        super.onResume()
        isFragmentVisible = true
    }

    override fun onPause() {
        super.onPause()
        isFragmentVisible = false
        if (cleaningDialog != null && cleaningDialog?.isShowing == true) {
            cleaningDialog?.dismiss()
            dialogShowing = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}
