package com.example.magnifierapp.fragment.homeFragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.magnifierapp.databinding.FragmentHomeBinding
import com.example.magnifierapp.fragment.camerax.YuvToRgbConverter
import com.example.utils.gone
import com.example.utils.toast
import com.example.utils.visible
import com.google.android.material.internal.FlowLayout
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint
import jp.co.cyberagent.android.gpuimage.GPUImageView
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding
    private val viewModel: HomeViewModel by viewModels()
    private var adapter: FilterAdapter? = null
    private var isFilterClicked = true

    companion object {
        const val REQUEST_CODE_PERMISSIONS = 10
    }

    private lateinit var gpuImageView: GPUImageView
    private var cameraProvider: ProcessCameraProvider? = null
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    private lateinit var converter: YuvToRgbConverter
    private var bitmap: Bitmap? = null
    private lateinit var zoomSlider: Slider
    private lateinit var cameraControl: CameraControl




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = FilterAdapter(filterList = listOf(), onItemClick = { itemAndPosition ->
            val (filterDataModel, position) = itemAndPosition
            println("Item clicked: ${filterDataModel.filterImage} at position $position")
            activity?.toast("position $position")


        })

        converter = YuvToRgbConverter(context?:return)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
                binding?.drawerLayout?.closeDrawer(GravityCompat.START)
            } else {
                showExitDialog()
            }
        }
        binding?.filterRecyclerView?.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        if (binding?.filterRecyclerView?.adapter == null) {
            binding?.filterRecyclerView?.adapter = adapter
        }

        gpuImageView= binding?.cameraPreview!!
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
            }
            navDrawer.viewFeedback.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            navDrawer.viewMoreApps.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            navDrawer.viewPrivacyPolicy.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
            }

            icFilter.setOnClickListener {
                if (isFilterClicked){
                    groupRecyclerView.visible()
                    verticalSeekbar.gone()
                }else{
                    groupRecyclerView.gone()
                    verticalSeekbar.visible()
                }
                isFilterClicked = !isFilterClicked
            }


        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun showExitDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Exit App")
        builder.setMessage("Do you want to exit the app?")
        builder.setPositiveButton("Yes") { dialog, _ ->
            dialog.dismiss()
            requireActivity().finish()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.create().show()
    }

    private fun cameraXSetup(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context?:return)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            startCameraIfReady()
        }, ContextCompat.getMainExecutor(context?:return))

//        zoomSlider.addOnChangeListener { _, value, _ ->
//            cameraControl.setLinearZoom(value)
//        }
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    @SuppressLint("UnsafeExperimentalUsageError")
    private fun startCameraIfReady() {

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
        cameraControl = camera?.cameraControl?:return
    }

    private fun allocateBitmapIfNecessary(width: Int, height: Int): Bitmap {
        if (bitmap == null || bitmap!!.width != width || bitmap!!.height != height) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        }
        return bitmap!!
    }




}