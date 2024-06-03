package com.example.junkcleaner.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.example.junkcleaner.Fragment.ColdShowerProcessManagFragment
import com.example.junkcleaner.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class   ProcessManagerActivity : AppCompatActivity() {

    private lateinit var backBtn:ImageView
    private lateinit var scanningMemory:LottieAnimationView
    private lateinit var scanningMenoryTextView:TextView
    private lateinit var SuccessfullLoader:LottieAnimationView
    private lateinit var processContainer:FrameLayout
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process_manager)
        backBtn=findViewById(R.id.backBtn)
        scanningMemory=findViewById(R.id.scanningMenory)
        scanningMenoryTextView=findViewById(R.id.scanningMenoryTextView)
        SuccessfullLoader=findViewById(R.id.SuccessfullLoader)
        processContainer=findViewById(R.id.processContainer)

        backBtn.setOnClickListener {
            finish()
        }
        startMemoryScan()
    }
    private fun startMemoryScan() {
        // Start the scanning animation
        scanningMemory.playAnimation()
        scanningMemory.visibility = LottieAnimationView.VISIBLE
        scanningMenoryTextView.visibility=TextView.VISIBLE
        SuccessfullLoader.visibility = LottieAnimationView.GONE

        coroutineScope.launch {
            // Simulate scanning task
            val result = simulateScanTask()
            // Update UI after task completion
            updateUIAfterScan(result)
        }
    }
    private suspend fun simulateScanTask(): Boolean {
        // Simulate a scanning process
        delay(5000) // Simulate a delay for 5 seconds
        return true // Return true indicating success
    }

    private fun updateUIAfterScan(success: Boolean) {
        if (success) {
            // Stop the scanning animation and show the successful animation
            scanningMemory.visibility = LottieAnimationView.GONE
            scanningMenoryTextView.visibility=TextView.GONE
            SuccessfullLoader.visibility = LottieAnimationView.VISIBLE
            SuccessfullLoader.playAnimation()

            coroutineScope.launch {
                val result=simulateScan()
                loadMemoryDetailsFragment(result)
            }
        }
    }

    private suspend fun simulateScan(): Boolean {
        // Simulate a scanning process
        delay(2000) // Wait for 2 seconds while successful animation is shown
        return true // Return true indicating success
    }

    private fun loadMemoryDetailsFragment(success: Boolean) {
        if(success){
            processContainer.visibility=FrameLayout.VISIBLE
            SuccessfullLoader.visibility=LottieAnimationView.GONE

            supportFragmentManager.beginTransaction().replace(
                R.id.processContainer,
                ColdShowerProcessManagFragment()
            ).commit()
        }
    }
}