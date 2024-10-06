package com.example.magnifierapp.fragment.splashFragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
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
import com.example.utils.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding
    private var cameraPermissionDeniedCount = 0
    private var isPermissionGranted = false // Flag to track if permission is granted
    private lateinit var requestCameraPermission: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Camera permission request launcher
        requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, set the flag to true
                isPermissionGranted = true
                cameraPermissionDeniedCount = 0
                Log.e("Permission", "Camera permission granted")
            } else {
                // Permission denied
                cameraPermissionDeniedCount++
                if (cameraPermissionDeniedCount >= 2 && !shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                    // User selected "Don't ask again", show system settings dialog
                    showPermissionSettingsDialog()
                } else if (cameraPermissionDeniedCount >= 2) {
                    // Show rationale dialog after second denial
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
                    // Navigate to the next screen if permission is already granted
                    Log.e("click", "Permission granted, navigating to HomeFragment")
                    if (findNavController().currentDestination?.id == R.id.splashFragment) {
                        findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                    }
                } else {
                    // Request camera permission if not granted
                    Log.e("click", "Requesting camera permission")
                    requestCameraPermission.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Check if the camera permission is granted when returning from settings
        isPermissionGranted = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

        // Update the flag, but do not navigate. Navigation will happen on button click.
        Log.e("onResume", "Permission granted flag updated: $isPermissionGranted")
    }

    private fun showPermissionRationaleDialog() {
        // Show a custom rationale dialog explaining why the permission is needed
        AlertDialog.Builder(requireContext())
            .setTitle("Camera Permission Required")
            .setMessage("This app requires camera access to take pictures. Please grant the permission.")
            .setPositiveButton("Allow") { _, _ ->
                // Request the permission again
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPermissionSettingsDialog() {
        // Show a dialog directing user to system settings
        AlertDialog.Builder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Camera permission has been permanently denied. Please allow the permission from the app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                // Navigate to app settings
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
