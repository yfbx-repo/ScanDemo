package com.ypc.scandemo

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.android.synthetic.main.activity_scan.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * Date: 2022-03-16
 * Author: Edward
 * Desc:
 */
class GoogleScanActivity : AppCompatActivity() {


    private lateinit var cameraExecutor: ExecutorService
    private var enableAnalyze = true
    private var continuous = true

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .build()
    )

    private val imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        info_text.movementMethod = ScrollingMovementMethod.getInstance()

        cameraExecutor = Executors.newSingleThreadExecutor()
        imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeBitmap)
        previewView.startCamera(this, imageAnalysis)
        previewView.setOnClickListener {
            startAnalyze()
        }
    }


    private fun startAnalyze() {
        enableAnalyze = true

    }

    private fun stopAnalyze() {
        enableAnalyze = false
    }


    /**
     * 谷歌图片分析
     */
    @SuppressLint("UnsafeOptInUsageError")
    private fun analyzeBitmap(imageProxy: ImageProxy) {
        if (!enableAnalyze) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image ?: return
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener(this::onSuccess)
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    /**
     * 分析结果
     */
    private fun onSuccess(result: List<Barcode>) {
        if (result.isEmpty()) return

        if (!continuous) {
            stopAnalyze()
        }

        val value = result.joinToString { it.displayValue!! }
        showResult(value)
    }

    /**
     * 启动相机
     */
    private fun PreviewView.startCamera(
        lifecycleOwner: LifecycleOwner,
        imageAnalysis: ImageAnalysis
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(surfaceProvider)
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis,
            )
        }, ContextCompat.getMainExecutor(context))
    }


    private fun showResult(result: String) {
        info_text.append(result)
        info_text.append("\n\n")

        val offset = info_text.lineCount * info_text.lineHeight
        if (offset > info_text.height) {
            info_text.scrollTo(0, offset - info_text.height + info_text.lineHeight * 2)
        }
    }

    override fun onDestroy() {
        scanner.close()
        cameraExecutor.shutdown()
        super.onDestroy()
    }


}