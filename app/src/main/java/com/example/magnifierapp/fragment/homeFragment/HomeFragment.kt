package com.example.magnifierapp.fragment.homeFragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.extensions.enableTapToFocus
import com.example.extensions.onKeyDown
import com.example.extensions.setZoom
import etech.magnifierplus.R
import com.example.magnifierapp.camerax.YuvToRgbConverter
import etech.magnifierplus.databinding.FragmentHomeBinding
import com.example.magnifierapp.dialog.RateUsDialog
import com.example.utils.ImageSaver
import com.example.utils.feedBackWithEmail
import com.example.utils.gone
import com.example.utils.moreApps
import com.example.utils.privacyPolicyUrl
import com.example.utils.visible
import dagger.hilt.android.AndroidEntryPoint
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBrightnessFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageColorInvertFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageContrastFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGlassSphereFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSketchFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageSolarizeFilter
import jp.co.cyberagent.android.gpuimage.util.Rotation
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


@AndroidEntryPoint
class HomeFragment : Fragment() {
    var zoomLevel: Int = 0
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding
    private val viewModel: HomeViewModel by viewModels()
    private var adapter: FilterAdapter? = null
    private var isFilterClicked = true
    private var cameraProvider: ProcessCameraProvider? = null
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var converter: YuvToRgbConverter
    private var bitmap: Bitmap? = null
    lateinit var cameraControl: CameraControl
    private var rateUsDialog: RateUsDialog? = null
    private var isFlashOn = false
    private var brightnessFilter = GPUImageBrightnessFilter(0f)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rateUsDialog = RateUsDialog(activity ?: return)
        adapter = FilterAdapter(filterList = listOf(), onItemClick = { itemAndPosition ->
            val (filterDataModel, position) = itemAndPosition
            println("Item clicked: ${filterDataModel.filterImage} at position $position")
            when (position) {
                0 -> {
                    binding?.cameraPreview?.filter = GPUImageFilter()
                }

                1 -> {
                    binding?.cameraPreview?.filter = GPUImageSketchFilter()
                }

                2 -> {
                    binding?.cameraPreview?.filter = GPUImageColorInvertFilter()
                }

                3 -> {
                    binding?.cameraPreview?.filter = GPUImageSolarizeFilter()
                }

                4 -> {
                    binding?.cameraPreview?.filter = GPUImageGrayscaleFilter()
                }

                5 -> {
                    binding?.cameraPreview?.filter = GPUImageBrightnessFilter(0.8f)
                }

                6 -> {
                    binding?.cameraPreview?.filter = GPUImageContrastFilter(2f)
                }

                7 -> {
                    binding?.cameraPreview?.filter = GPUImageGlassSphereFilter()
                }
            }


        })

        converter = YuvToRgbConverter(context ?: return)

    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (findNavController().currentDestination?.id == R.id.homeFragment) {
                    showExitDialog()
                }
                else {
                    findNavController().popBackStack()
                }
            }
        })

        binding?.filterRecyclerView?.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        if (binding?.filterRecyclerView?.adapter == null) {
            binding?.filterRecyclerView?.adapter = adapter
        }

        clickListener()
        observerViewModel()
        cameraXSetup()
    }

    private fun observerViewModel() {
        viewModel.filterList.observe(viewLifecycleOwner) { filterList ->
            if (!filterList.isNullOrEmpty()) {
                adapter?.updateFilterList(filterList)
            }
        }
    }

    private fun clickListener() {
        binding?.run {
            icCamera.setOnClickListener {
                saveImage()
            }
            icMenu.setOnClickListener {
                try {
                    if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                        drawerLayout.open()
                    } else {
                        drawerLayout.close()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            navDrawer.viewRemoveAds.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            navDrawer.viewRateUs.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
                rateUsDialog?.show()
            }
            navDrawer.viewFeedback.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
                activity?.feedBackWithEmail("Feedback", "Any Feedback", "unknow@gmail.com")
            }
            navDrawer.viewMoreApps.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
                activity?.moreApps()
            }
            navDrawer.viewPrivacyPolicy.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
                activity?.privacyPolicyUrl()
            }
            val packageInfo = context?.packageManager?.getPackageInfo(context?.packageName!!, 0)
            val versionName = packageInfo?.versionName
            navDrawer.txtVersion.text = "Version: $versionName"

            icFilter.setOnClickListener {
                if (isFilterClicked) {
                    groupRecyclerView.visible()
                    verticalSeekbar.gone()
                } else {
                    groupRecyclerView.gone()
                    verticalSeekbar.visible()
                }
                isFilterClicked = !isFilterClicked
            }
            verticalSeekbar.apply {
                max = 100
                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar, progress: Int, fromUser: Boolean
                    ) {
                        setZoom(binding, progress)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
            }
            binding?.horizontalSeekbar?.apply {
                max = 100
                progress = 10

                setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                        val brightnessValue = (progress - 50) / 50f
                        brightnessFilter.setBrightness(brightnessValue)
                        binding?.cameraPreview?.filter = brightnessFilter
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
            }
            icFlash.setOnClickListener {
                isFlashOn = !isFlashOn
                cameraControl.enableTorch(isFlashOn)
                if (isFlashOn) {
                    binding?.icFlash?.setImageResource(R.drawable.flash_off)
                } else {
                    binding?.icFlash?.setImageResource(R.drawable.flash_on__1_)
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraProvider?.unbindAll()
    }
    private fun getRotation(orientation: Int): Rotation {
        return when (orientation) {
            90 -> Rotation.ROTATION_90
            180 -> Rotation.ROTATION_180
            270 -> Rotation.ROTATION_270
            else -> Rotation.NORMAL
        }
    }

    fun getCameraOrientation(): Int {
        val degrees = when (activity?.windowManager?.defaultDisplay?.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        return (90 - degrees) % 360

    }


    private fun showExitDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Exit App")
            setMessage("Do you want to exit the app?")
            setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                requireActivity().finish() // app ko close kar dega
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss() // dialog ko close kar dega
            }
            create()
            show()
        }
    }
    private fun cameraXSetup() {
        binding?.cameraPreview?.setRotation(getRotation(getCameraOrientation()))
        binding?.cameraPreview?.setScaleType(GPUImage.ScaleType.CENTER_CROP)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context ?: return)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            startCameraIfReady()
        }, ContextCompat.getMainExecutor(context ?: return))
    }

    @OptIn(ExperimentalGetImage::class)
    @SuppressLint("UnsafeExperimentalUsageError")
    private fun startCameraIfReady() {
        val imageAnalysis =
            ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
        imageAnalysis.setAnalyzer(executor) { imageProxy ->
            val bitmap = allocateBitmapIfNecessary(imageProxy.width, imageProxy.height)
            imageProxy.image?.let { bitmap?.let { it1 -> converter.yuvToRgb(it, it1) } }
            imageProxy.close()
            binding?.cameraPreview?.apply {
                post {
                    setImage(bitmap)
                }
            }
        }
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val camera = cameraProvider?.bindToLifecycle(this, cameraSelector, imageAnalysis)
        cameraControl = camera?.cameraControl ?: return
        binding?.verticalSeekbar?.progress = 2
        cameraControl.setLinearZoom(0.0f)
        binding?.icZoom?.text = getString(R.string.x1second)
        binding?.cameraPreview?.let {
            binding?.focusIndicator?.let { it1 ->
                enableTapToFocus(
                    gpuImageView = it,
                    focusIndicatorView = it1
                )
            }
        }
    }

    private fun allocateBitmapIfNecessary(width: Int, height: Int): Bitmap? {
        if (bitmap == null || bitmap?.width != width || bitmap?.height != height) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
        return bitmap
    }

    private fun saveImage() {
        ImageSaver.saveImage(bitmap, requireContext()) { uri ->
            Toast.makeText(context ?: return@saveImage, "Saved: $uri", Toast.LENGTH_SHORT).show()
            val bundle = Bundle()
            bundle.putString("imageUri", uri.toString())
            findNavController().navigate(R.id.action_homeFragment_to_displayImageFragment, bundle)

        }
    }

    // Adjust the brightness based on user input
    private fun adjustBrightness(brightnessValue: Int) {
        try {
            Settings.System.putInt(
                requireContext().contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightnessValue
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(), "Permission required to change brightness", Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun handleVolumeKeys(keyCode: Int): Boolean {
        return onKeyDown(binding = binding, keyCode = keyCode)
    }
}