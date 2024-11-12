package com.example.extensions

import android.annotation.SuppressLint
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.lifecycle.lifecycleScope
import com.example.magnifierapp.fragment.homeFragment.HomeFragment
import etech.magnifierplus.R
import etech.magnifierplus.databinding.FragmentHomeBinding
import jp.co.cyberagent.android.gpuimage.GPUImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun HomeFragment.onKeyDown(binding: FragmentHomeBinding?, keyCode: Int): Boolean {
    when (keyCode) {
        KeyEvent.KEYCODE_VOLUME_UP -> {
            if (zoomLevel < 100) {
                zoomLevel += 5
                binding?.verticalSeekbar?.progress = zoomLevel
                setZoom(binding, zoomLevel)
            }
            return true
        }

        KeyEvent.KEYCODE_VOLUME_DOWN -> {
            if (zoomLevel > 0) {
                zoomLevel -= 5
                binding?.verticalSeekbar?.progress = zoomLevel
                setZoom(binding, zoomLevel)
            }
            return true
        }
    }
    return false
}

fun HomeFragment.setZoom(binding: FragmentHomeBinding?, progress: Int) {
    zoomLevel = progress
    val zoomValue = zoomLevel / 100.0f
    Log.e("ZoomValue", "Progress: $zoomLevel -> Zoom: $zoomValue")
    cameraControl.setLinearZoom(zoomValue)
    viewLifecycleOwner.lifecycleScope.launch {
        val zoomValueText = 1 + (zoomLevel / 10.0).toInt()
        withContext(Dispatchers.Main) {
            binding?.icZoom?.text = getString(R.string.zoom_level_text, zoomValueText.toString())
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
fun HomeFragment.enableTapToFocus(
    gpuImageView: GPUImageView, focusIndicatorView: AppCompatImageView
) {
    gpuImageView.setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_UP) {
            val factory = SurfaceOrientedMeteringPointFactory(
                gpuImageView.width.toFloat(), gpuImageView.height.toFloat()
            )
            val meteringPoint = factory.createPoint(event.x, event.y)
            val focusMeteringAction = FocusMeteringAction.Builder(meteringPoint).build()
            Log.e("enableTapToFocus", "enableTapToFocus:${focusMeteringAction}")
            cameraControl.startFocusAndMetering(focusMeteringAction)
            focusIndicatorView.visibility = View.VISIBLE
            focusIndicatorView.x = event.x - (focusIndicatorView.width / 2)
            focusIndicatorView.y = event.y - (focusIndicatorView.height / 2)
            focusIndicatorView.animate().alpha(0f).setDuration(2000).withEndAction {
                focusIndicatorView.visibility = View.GONE
                focusIndicatorView.alpha = 1f
            }.start()
        }
        true
    }
}