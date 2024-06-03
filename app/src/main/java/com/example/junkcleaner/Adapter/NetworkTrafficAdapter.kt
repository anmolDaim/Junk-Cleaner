package com.example.junkcleaner.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.junkcleaner.DataClass.networkTrafficDataClass
import com.example.junkcleaner.databinding.NetworkTrafficLayoutBinding

class NetworkTrafficAdapter(val networkList:List<networkTrafficDataClass>):RecyclerView.Adapter<NetworkTrafficAdapter.ViewHolder>() {

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

        holder.binding.networkButton.setOnClickListener {
            // Handle button click
        }
    }



}