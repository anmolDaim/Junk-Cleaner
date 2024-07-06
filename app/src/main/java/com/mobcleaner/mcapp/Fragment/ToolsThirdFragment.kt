package com.mobcleaner.mcapp.Fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.mobcleaner.mcapp.Activity.MainActivity
import com.mobcleaner.mcapp.DataClass.junkFiles
import com.mobcleaner.mcapp.R
import com.mobcleaner.mcapp.databinding.FragmentToolsThirdBinding
import kotlinx.coroutines.*
import java.io.File
import kotlin.coroutines.CoroutineContext

class ToolsThirdFragment : Fragment(), CoroutineScope {

    private lateinit var binding: FragmentToolsThirdBinding
    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private val handler = Handler()
    private val progressReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val progress = intent?.getIntExtra("progress", 0) ?: 0
            updateProgress(progress)
        }
    }


    private var isFragmentVisible = false
    private var cleaningDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentToolsThirdBinding.inflate(inflater, container, false)

        // Register the receiver
        val filter = IntentFilter("com.example.junkcleaner.UPDATE_PROGRESS")
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(progressReceiver, filter)

        // Retrieve junk files from arguments
        val junkFiles = arguments?.getSerializable("junkFiles") as? List<junkFiles>
        junkFiles?.let {
            Log.d("ToolsThirdFragment", "Starting to clean ${it.size} junk files")
            cleanJunkFiles(it)
        }

        return binding.root
    }
    override fun onResume() {
        super.onResume()
        isFragmentVisible = true
    }

    override fun onPause() {
        super.onPause()
        isFragmentVisible = false
        cleaningDialog?.dismiss()
    }
    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(progressReceiver)
        job.cancel()
    }

    private fun updateProgress(progress: Int) {
        binding.progressBar.progress = progress
        binding.txtProgress.text = "$progress %"
    }

    private fun cleanJunkFiles(junkFiles: List<junkFiles>) {
        launch(Dispatchers.IO) {
            for ((index, junkFile) in junkFiles.withIndex()) {
                val filePath = junkFile.junkFileName
                try {
                    val file = File(filePath)
                    if (file.exists()) {
                        if (file.delete()) {
                            Log.d("ToolsThirdFragment", "Deleted file: $filePath")
                        } else {
                            Log.e("ToolsThirdFragment", "Failed to delete file: $filePath")
                        }
                    } else {
                        Log.e("ToolsThirdFragment", "File does not exist: $filePath")
                    }
                } catch (e: Exception) {
                    Log.e("ToolsThirdFragment", "Error deleting file: $filePath", e)
                }

                val progress = ((index + 1) / junkFiles.size.toFloat() * 100).toInt()

                // Send progress update via local broadcast
                val intent = Intent("com.example.junkcleaner.UPDATE_PROGRESS")
                intent.putExtra("progress", progress)
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)

                delay(100) // Simulate time taken to delete
            }
            withContext(Dispatchers.Main) {
                if (isFragmentVisible) {
                    Toast.makeText(requireContext(), "Cleaning completed", Toast.LENGTH_SHORT).show()
                    showDialog()

                    binding.progressBar2.visibility = View.GONE
                    binding.progressBar3.visibility = View.GONE
                    binding.progressBar4.visibility = View.GONE
                }
            }
        }
    }

    private fun showDialog() {
            val builder = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogStyle)
            val inflater = LayoutInflater.from(requireContext())
            val dialogView = inflater.inflate(R.layout.successfull_cleaning_layout, null)
            builder.setView(dialogView)

            val doneButton: AppCompatButton =
                dialogView.findViewById(R.id.doneButton) // New button for navigating to MainActivity
            val dialog = builder.create()

            doneButton.setOnClickListener {
                dialog.dismiss() // Close the dialog first
                val mainActivityIntent = Intent(requireContext(), MainActivity::class.java)
                startActivity(mainActivityIntent)
                requireActivity().finish()
            }

            dialog.show()
    }

}
