package com.mobcleaner.mcapp.Fragment

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import com.mobcleaner.mcapp.Activity.MainActivity
import com.mobcleaner.mcapp.databinding.FragmentToolsSecondBinding

class ToolsSecondFragment : Fragment() {

    private var _binding: FragmentToolsSecondBinding? = null
    private val binding get() = _binding!!
    private var dialog: AlertDialog? = null
    private var dialogShowing = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentToolsSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.startTestButton.setOnClickListener {
            binding.speedometer.visibility = View.VISIBLE
            binding.downloadSpeedTextView.visibility = View.VISIBLE
            binding.uploadSpeedTextView.visibility = View.VISIBLE
            binding.downlad.visibility = View.VISIBLE
            binding.upload.visibility = View.VISIBLE
            binding.imageView9.visibility = View.VISIBLE
            binding.imageView13.visibility = View.VISIBLE
            binding.view16.visibility = View.VISIBLE
            binding.lottliWaves.visibility = View.VISIBLE
            binding.startTestButton.visibility = View.GONE
            binding.lottlieBtn.visibility = View.GONE
            getNetworkSpeed()
        }
    }

    private fun getNetworkSpeed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val cm = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val nc = cm.getNetworkCapabilities(cm.activeNetwork)
            if (nc != null) {
                val downSpeedKbps = nc.linkDownstreamBandwidthKbps
                val upSpeedKbps = nc.linkUpstreamBandwidthKbps

                // Convert Kbps to Mbps
                val downSpeedMbps = downSpeedKbps / 1000.0
                val upSpeedMbps = upSpeedKbps / 1000.0

                Log.d("speed", "Download Speed: $downSpeedMbps Mbps")
                Log.d("speed", "Upload Speed: $upSpeedMbps Mbps")

                binding.speedometer.setSpeed(downSpeedMbps.toInt(), 1000L)
                // Update the UI with the speed values
                binding.downlad.text = String.format("%.2f Mbps", downSpeedMbps)
                binding.upload.text = String.format("%.2f Mbps", upSpeedMbps)

                Handler(Looper.getMainLooper()).postDelayed({
                    if (isAdded && activity != null) { // Check if fragment is added and activity is not null
                        showSpeedDialog(downSpeedMbps, upSpeedMbps)
                    }
                }, 3000)
            } else {
                Log.d("speed", "Network capabilities are null")
            }
        } else {
            openSpeedTestInBrowser()
        }
    }

    private fun showSpeedDialog(downSpeed: Double, upSpeed: Double) {
        if (!dialogShowing) {
            val dialogView = LayoutInflater.from(requireContext()).inflate(com.mobcleaner.mcapp.R.layout.network_dialog, null)

            dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()
            dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialogView.findViewById<AppCompatButton>(com.mobcleaner.mcapp.R.id.homeBtn).setOnClickListener {
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                dialog?.dismiss()
            }
            dialogView.findViewById<TextView>(com.mobcleaner.mcapp.R.id.downloadMbps).text = downSpeed.toString()

            // Check again if fragment is added before showing the dialog
            if (isAdded && activity != null) {
                dialog?.show()

                dialog?.setOnDismissListener {
                    dialogShowing = false
                }

                dialogShowing = true
            }
        }
    }

    private fun moveInBrowser() {
        val speedTestUrl = "https://www.speedtest.net/"
        try {
            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.launchUrl(requireContext(), Uri.parse(speedTestUrl))
        } catch (e: Exception) {
            // If Custom Tabs is not supported, fall back to regular browser intent
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(speedTestUrl))
            startActivity(browserIntent)
        }
    }

    private fun openSpeedTestInBrowser() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("Network Speed Test")
        dialogBuilder.setMessage(
            "Your device is below android version 10, Navigate to chrome tab for checking network speed")
        dialogBuilder.setPositiveButton("open") { dialog, _ ->
            moveInBrowser()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    override fun onPause() {
        super.onPause()
        dialog?.dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
