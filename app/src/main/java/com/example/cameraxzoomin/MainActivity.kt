package com.example.cameraxzoomin

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.widget.Button
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.android.material.slider.Slider
import com.nex3z.flowlayout.FlowLayout
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorInvertFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageCrosshatchFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGammaFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGlassSphereFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImagePixelationFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSketchFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSolarizeFilter
import jp.co.cyberagent.android.gpuimage.util.Rotation
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 10
    }

    private lateinit var gpuImageView: GPUImageView
    private var cameraProvider: ProcessCameraProvider? = null
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var converter: YuvToRgbConverter
    private var bitmap: Bitmap? = null
    private lateinit var zoomSlider: Slider
    private lateinit var buttonContainer: FlowLayout
    private lateinit var cameraControl: CameraControl


    private fun getRotation(orientation: Int): Rotation {
        return when (orientation) {
            90 -> Rotation.ROTATION_90
            180 -> Rotation.ROTATION_180
            270 -> Rotation.ROTATION_270
            else -> Rotation.NORMAL
        }
    }

    fun getCameraOrientation(): Int {
        val degrees = when (windowManager.defaultDisplay.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        return (90 - degrees) % 360

//        return if (false) {
//            (90 + degrees) % 360
//        } else { // back-facing
//            (90 - degrees) % 360
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        converter = YuvToRgbConverter(this)

        gpuImageView = findViewById(R.id.gpu_image_view)
        buttonContainer = findViewById(R.id.button_container)
        zoomSlider = findViewById(R.id.zoom_slider)
        addButtons()

        gpuImageView.setRotation(getRotation(getCameraOrientation()))
        gpuImageView.setScaleType(GPUImage.ScaleType.CENTER_CROP)
        requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_PERMISSIONS)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            startCameraIfReady()
        }, ContextCompat.getMainExecutor(this))
        zoomSlider.addOnChangeListener { _, value, _ ->
            Log.e("ThisIsSattiii----->", "onCreate: ${value}", )
            cameraControl.setLinearZoom(value)
        }
    }

    private fun addButtons() {
        addButton("No Filter", GPUImageFilter())
        addButton("Sketch", GPUImageSketchFilter())
        addButton("Color Invert", GPUImageColorInvertFilter())
        addButton("Solarize", GPUImageSolarizeFilter())
        addButton("Grayscale", GPUImageGrayscaleFilter())
        addButton("Brightness", GPUImageBrightnessFilter(0.8f))
        addButton("Contrast", GPUImageContrastFilter(2f))
        addButton("Pixelation", GPUImagePixelationFilter().apply { setPixel(20F) })
        addButton("Glass Sphere", GPUImageGlassSphereFilter())
        addButton("Crosshatch", GPUImageCrosshatchFilter())
        addButton("Gamma", GPUImageGammaFilter(2f))
    }

    private fun addButton(text: String, filter: GPUImageFilter?) {
        val button = Button(this).apply {
            setText(text)
            setOnClickListener {
                gpuImageView.filter = filter
            }
        }
        buttonContainer.addView(button)

    }

    @OptIn(ExperimentalGetImage::class)
    @SuppressLint("UnsafeExperimentalUsageError")
    private fun startCameraIfReady() {
        if (!isPermissionsGranted() || cameraProvider == null) {
            return
        }

        // Set up image analysis
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(executor) { imageProxy ->
            val bitmap = allocateBitmapIfNecessary(imageProxy.width, imageProxy.height)
            converter.yuvToRgb(imageProxy.image!!, bitmap)
            imageProxy.close()
            gpuImageView.post {
                gpuImageView.setImage(bitmap)
            }
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val camera = cameraProvider?.bindToLifecycle(this, cameraSelector, imageAnalysis)
        cameraControl = camera?.cameraControl ?: return

    }

    private fun allocateBitmapIfNecessary(width: Int, height: Int): Bitmap {
        if (bitmap == null || bitmap!!.width != width || bitmap!!.height != height) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
        return bitmap!!
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            startCameraIfReady()
        }
    }

    private fun isPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
}
//class MainActivity : AppCompatActivity() {
//    private lateinit var cameraControl: CameraControl
//    private lateinit var camera: androidx.camera.core.Camera
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        val previewView = findViewById<androidx.camera.view.PreviewView>(R.id.previewView)
//        val zoomSlider = findViewById<Slider>(R.id.zoomSlider)
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//        cameraProviderFuture.addListener({
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//            val preview = Preview.Builder().build()
//            preview.setSurfaceProvider(previewView.surfaceProvider)
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//            camera = cameraProvider.bindToLifecycle(
//                this, cameraSelector, preview
//            )
//            cameraControl = camera.cameraControl
//
//
//
//            zoomSlider.addOnChangeListener { _, value, _ ->
//                cameraControl.setLinearZoom(value / zoomSlider.valueTo)
//            }
//
//        }, ContextCompat.getMainExecutor(this))
//    }
//}