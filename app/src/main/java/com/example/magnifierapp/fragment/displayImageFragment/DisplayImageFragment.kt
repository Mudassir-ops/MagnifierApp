package com.example.magnifierapp.fragment.displayImageFragment

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.magnifierapp.databinding.FragmentDisplayImageBinding
import com.example.magnifierapp.fragment.homeFragment.FilterAdapter
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
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
class DisplayImageFragment : Fragment() {
    private var _binding: FragmentDisplayImageBinding? = null
    private val binding get() = _binding
    private var adapter: FilterAdapter? = null
    private val viewModel: DisplayViewModel by viewModels()

    private val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = FilterAdapter(filterList = listOf(), onItemClick = { itemAndPosition ->
            val (filterDataModel, position) = itemAndPosition
            when (position) {
                0 -> binding?.icImage?.filter = GPUImageFilter()
                1 -> binding?.icImage?.filter = GPUImageSketchFilter()
                2 -> binding?.icImage?.filter = GPUImageColorInvertFilter()
                3 -> binding?.icImage?.filter = GPUImageSolarizeFilter()
                4 -> binding?.icImage?.filter = GPUImageGrayscaleFilter()
                5 -> binding?.icImage?.filter = GPUImageBrightnessFilter(0.8f)
                6 -> binding?.icImage?.filter = GPUImageContrastFilter(2f)
                7 -> binding?.icImage?.filter = GPUImageGlassSphereFilter()
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDisplayImageBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageUri = arguments?.getString("imageUri")
        if (imageUri != null) {
            binding?.apply {
                icImage.setRotation(getRotation(getCameraOrientation()))
                icImage.setImage(Uri.parse(imageUri))
                icImage.setScaleType(GPUImage.ScaleType.CENTER_CROP)
                icImage.setRatio(1f)
            }

        } else {
            Toast.makeText(requireContext(), "Image not found", Toast.LENGTH_SHORT).show()
        }

        binding?.filterRecyclerView?.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding?.filterRecyclerView?.adapter = adapter

        binding?.txtSave?.setOnClickListener {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                requestStoragePermission()
            } else {
                saveImage()
            }
        }
        binding?.icBackImage?.setOnClickListener {
            findNavController().navigateUp()
        }

        observerViewModel()
    }

    private fun observerViewModel() {
        viewModel.filterList.observe(viewLifecycleOwner) { filterList ->
            if (!filterList.isNullOrEmpty()) {
                adapter?.updateFilterList(filterList)
            }
        }
    }

    private fun getRotation(orientation: Int): Rotation {
        return when (orientation) {
            90 -> Rotation.ROTATION_90
            180 -> Rotation.ROTATION_180
            270 -> Rotation.ROTATION_270
            else -> Rotation.NORMAL
        }
    }

    private fun getCameraOrientation(): Int {
        val degrees = when (activity?.windowManager?.defaultDisplay?.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
        return (90 - degrees) % 360
    }

    // Function to request storage permission for Android 6 to 9
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            )
        } else {
            saveImage()
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, save the image
                saveImage()
            } else {
                Toast.makeText(requireContext(), "Storage permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImage() {
        val bitmap = binding?.icImage?.gpuImage?.bitmapWithFilterApplied
        bitmap?.let { saveImageToGallery(it) }
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        val filename = "IMG_${System.currentTimeMillis()}.jpg"

        // Android 10+ (API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyAppFolder")
            }

            val resolver = requireContext().contentResolver
            val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            imageUri?.let {
                val outputStream = resolver.openOutputStream(it)
                outputStream?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.flush()
                    Toast.makeText(requireContext(), "Image saved in gallery", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // Below Android 10 (API 28 and below)
        else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/MagnifierAutonomousHub"
            val file = File(imagesDir)
            if (!file.exists()) {
                file.mkdirs()
            }
            val imageFile = File(file, filename)
            val outputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Notify the gallery
            MediaScannerConnection.scanFile(
                requireContext(),
                arrayOf(imageFile.toString()),
                arrayOf("image/jpeg"),
            ) { _, _ -> }

            Toast.makeText(requireContext(), "Image saved in gallery", Toast.LENGTH_SHORT).show()
        }
    }
}
