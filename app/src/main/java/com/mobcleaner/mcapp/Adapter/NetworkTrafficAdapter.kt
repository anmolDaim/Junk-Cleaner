package com.mobcleaner.mcapp.Adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.mobcleaner.mcapp.DataClass.networkTrafficDataClass
import com.mobcleaner.mcapp.R
import com.mobcleaner.mcapp.databinding.NetworkTrafficLayoutBinding

class NetworkTrafficAdapter(val networkList:List<networkTrafficDataClass>,val context: Context):RecyclerView.Adapter<NetworkTrafficAdapter.ViewHolder>() {

    class ViewHolder(val binding:NetworkTrafficLayoutBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NetworkTrafficLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return networkList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appData = networkList[position]
        holder.binding.networkTrafficImage.setImageDrawable(appData.icon)
        holder.binding.networkTrafficName.text = appData.appName
        holder.binding.networkTrafficDatatUsage.text = appData.dataUsage.toString()

        val mobileDataUsage = appData.mobileDataUsage // Value in percentage (0-100)
        val wifiDataUsage = appData.wifiDataUsage // Value in percentage (0-100)

        holder.binding.progressBar3.progress = wifiDataUsage
        holder.binding.progressBar3.secondaryProgress = wifiDataUsage + mobileDataUsage

        // Check if data usage is more than 500 MB
        val dataUsageStr = appData.dataUsage
        val regexPattern = Regex("[\\d.]+")
        val matchResult = regexPattern.find(dataUsageStr)
        val usageInBytes = matchResult?.value?.toDoubleOrNull()
        val usageType = dataUsageStr.takeLast(2).trim()

        if (usageType == "GB" && usageInBytes != null && usageInBytes > 2.0) {
            // Convert GB to KB for comparison (1 GB = 1024 MB = 1024 * 1024 KB)
            val usageInKB = usageInBytes * 1024 * 1024
            if (usageInKB > 500 * 1024) {
                holder.binding.networkButton.setBackgroundResource(R.drawable.optimize_btn) // Set your background resource for more than 500 MB
                holder.binding.networkButton.setOnClickListener {
                    // Open app settings
                    val dialog = AlertDialog.Builder(context)

                        .setMessage("Tap Force Stop to quit the traffic-consuming app completely.")
                        .setPositiveButton("Got It") { _, _ ->
                            openAppSettings(context, appData.packageName)
                        }

                        .create()
                    dialog.show()
                }
            } else {
                // Hide button if data usage is not more than 500 MB
                holder.binding.networkButton.setBackgroundResource(R.drawable.remove_btn) // Replace with your drawable resource
            }
        } else if (usageType == "MB" && usageInBytes != null && usageInBytes > 500.0) {
            // Convert MB to KB for comparison (1 MB = 1024 KB)
            val usageInKB = usageInBytes * 1024
            if (usageInKB > 500 * 1024) {
                holder.binding.networkButton.setBackgroundResource(R.drawable.optimize_btn) // Set your background resource for more than 500 MB
                holder.binding.networkButton.setOnClickListener {
                    // Open app settings
                    val dialog = AlertDialog.Builder(context)

                        .setMessage("Tap Force Stop to quit the traffic-consuming app completely.")
                        .setPositiveButton("Got It") { _, _ ->
                            openAppSettings(context, appData.packageName)
                        }

                        .create()
                    dialog.show()

                }
            } else {
                // Hide button if data usage is not more than 500 MB
                holder.binding.networkButton.setBackgroundResource(R.drawable.remove_btn) // Replace with your drawable resource
            }
        }
    }
    private fun openAppSettings(context: Context, packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        context.startActivity(intent)
    }


}