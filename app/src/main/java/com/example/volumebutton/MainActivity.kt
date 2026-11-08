package com.example.volumebutton

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pad = (24 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(pad, pad, pad, pad)
        }

        root.addView(TextView(this).apply {
            text = "Chạm nút nổi để hiện thanh âm lượng.\nKéo nút để đổi vị trí. Dùng thanh bên dưới để chỉnh độ mờ."
            textSize = 16f
            gravity = Gravity.CENTER
        })

        val prefs = getSharedPreferences(FloatingService.PREFS, MODE_PRIVATE)
        val label = TextView(this).apply { textSize = 16f; gravity = Gravity.CENTER }
        val seek = SeekBar(this).apply {
            max = 80 // 20%..100%
            progress = prefs.getInt(FloatingService.KEY_OPACITY, 100) - 20
        }
        fun refreshLabel() { label.text = "Độ mờ nút: ${seek.progress + 20}%" }
        refreshLabel()
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                refreshLabel()
                prefs.edit().putInt(FloatingService.KEY_OPACITY, value + 20).apply()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        root.addView(label)
        root.addView(seek)

        root.addView(Button(this).apply {
            text = "Bật nút nổi"
            setOnClickListener { startFloating() }
        })

        root.addView(Button(this).apply {
            text = "Tắt nút nổi"
            setOnClickListener { stopService(Intent(this@MainActivity, FloatingService::class.java)) }
        })

        setContentView(root)
    }

    private fun startFloating() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Hãy cấp quyền hiển thị trên ứng dụng khác", Toast.LENGTH_LONG).show()
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
            return
        }
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        startForegroundService(Intent(this, FloatingService::class.java))
    }
}
