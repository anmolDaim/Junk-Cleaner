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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.junkcleaner.Activity.ColdShowerTempScannerActivity
import com.example.junkcleaner.R
import com.example.junkcleaner.databinding.BottomSheetLayoutBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class MyBottomSheetDialogFragment:BottomSheetDialogFragment() {
    private lateinit var binding:BottomSheetLayoutBinding
    private val PERMISSION_REQUEST_CODE = 101
    private val SPECIAL_PERMISSION_REQUEST_CODE = 102
    // SharedPreferences to store the permissions state
    private val PREFS_NAME = "AppManagerPrefs"
    private val PERMISSIONS_GRANTED = "permissions_granted"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=BottomSheetLayoutBinding.inflate(layoutInflater)

        binding.cancelBtn.setOnClickListener(){
            dismiss()
        }

        binding.grantbtn.setOnClickListener(){
// Check for permissions on activity start
            if (checkPermission()) {
               displayAllFiles()

            } else {
                requestPermission()
            }
        }
        return binding.root
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

    private fun displayAllFiles() {
        // Permissions are already granted
        val intent= Intent(requireContext(),ColdShowerTempScannerActivity::class.java)
        startActivity(intent)
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

}