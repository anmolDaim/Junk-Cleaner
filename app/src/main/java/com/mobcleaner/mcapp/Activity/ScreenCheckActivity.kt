package com.mobcleaner.mcapp.Activity

import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.mobcleaner.mcapp.R
import com.mobcleaner.mcapp.databinding.ActivityScreenCheckBinding

class ScreenCheckActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScreenCheckBinding
    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityScreenCheckBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener(){
            super.onBackPressed()
        }

        // Fetch and display screen resolution
        val displayMetrics = DisplayMetrics()
            this.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val width = displayMetrics.widthPixels
        val height = displayMetrics.heightPixels
        binding.screensize.text = "${width} x ${height}"

        binding.testBtn.setOnClickListener(){
            showColorDialog()
        }
    }

    private fun showColorDialog() {
        val dialogView = layoutInflater.inflate(R.layout.check_screen_dialog, null)
        val color1 = dialogView.findViewById<View>(R.id.red)
        val color2 = dialogView.findViewById<View>(R.id.green)
        val color3 = dialogView.findViewById<View>(R.id.blue)
        val color4 = dialogView.findViewById<View>(R.id.pink)
        val color5 = dialogView.findViewById<View>(R.id.yellow)
        val color6 = dialogView.findViewById<View>(R.id.skyBlue)
        val color7 = dialogView.findViewById<View>(R.id.black)
        val color8 = dialogView.findViewById<View>(R.id.white)


        // Set click listeners for each color view
        color1.setOnClickListener { changeBackgroundColor(Color.parseColor("#F44336")) } // Example color
        color2.setOnClickListener { changeBackgroundColor(Color.parseColor("#FF4CAF50")) } // Example color
        color3.setOnClickListener { changeBackgroundColor(Color.parseColor("#FF3F51B5")) } // Example color
        color4.setOnClickListener { changeBackgroundColor(Color.parseColor("#FFE91E63")) } // Example color
        color5.setOnClickListener { changeBackgroundColor(Color.parseColor("#FFFFC107")) } // Example color
        color6.setOnClickListener { changeBackgroundColor(Color.parseColor("#FF2196F3")) } // Example color
        color7.setOnClickListener { changeBackgroundColor(Color.parseColor("#FF000000")) } // Example color
        color8.setOnClickListener { changeBackgroundColor(Color.parseColor("#FFFFFFFF")) } // Example color

        val builder = AlertDialog.Builder(this, R.style.TransparentDialog)
            .setView(dialogView)
            .setCancelable(true)

        dialog = builder.create()
        dialog.show()
    }
    // Method to change activity background color
    private fun changeBackgroundColor(color: Int) {
        binding.bgColor.setBackgroundColor(color)
        dialog.dismiss() // Dismiss dialog after selecting a color
    }

}