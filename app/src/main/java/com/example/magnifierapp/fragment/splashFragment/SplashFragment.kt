package com.example.magnifierapp.fragment.splashFragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.magnifierapp.R
import com.example.magnifierapp.databinding.FragmentSplashBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding
    private var cameraPermissionDeniedCount = 0
    private var isPermissionGranted = false
    private lateinit var requestCameraPermission: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                isPermissionGranted = true
                cameraPermissionDeniedCount = 0
            } else {
                cameraPermissionDeniedCount++
                if (cameraPermissionDeniedCount >= 2 && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    showPermissionSettingsDialog()
                } else if (cameraPermissionDeniedCount >= 2) {
                    showPermissionRationaleDialog()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            btnSplash.setOnClickListener {
                if (isPermissionGranted) {
                    btnSplash.text = "Get Started"
                    if (findNavController().currentDestination?.id == R.id.splashFragment) {
                        findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                    }
                } else {
                    btnSplash.text = "Grant Permission"
                    requestCameraPermission.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isPermissionGranted = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        binding?.btnSplash?.text = if (isPermissionGranted) {
            "Get Started"
        } else {
            "Grant Permission"
        }

    }

    private fun showPermissionRationaleDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Camera Permission Required")
            .setMessage("This app requires camera access to take pictures. Please grant the permission.")
            .setPositiveButton("Allow") { _, _ ->
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPermissionSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Camera permission has been permanently denied. Please allow the permission from the app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireContext().packageName, null)
                intent.data = uri
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
