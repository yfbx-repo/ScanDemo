package com.ypc.scandemo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.ypcang.android.shop.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    private val CODE_PERMISSION = 1000
    private val CODE_SCAN = 1001

    private val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!permissions.all { hasGrant(it) }) {
            ActivityCompat.requestPermissions(this, permissions, CODE_PERMISSION)
        }


        /**
         * 华为统一扫码服务
         */
        hms_btn.setOnClickListener {
            startActivity(Intent(this, HmsScanActivity::class.java))
        }

        /**
         * Google 机器学习（文本识别、人脸识别、地标识别、条形码识别和图像标注）
         */
        google_btn.setOnClickListener {
            startActivity(Intent(this, GoogleScanActivity::class.java))
        }
    }
}


fun Context.hasGrant(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}