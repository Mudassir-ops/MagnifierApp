package com.example.magnifierapp.fragment.splashFragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.fragment.app.Fragment

fun showPermissionDialog(context: Context, fragment: Fragment) {
    val builder = AlertDialog.Builder(fragment.requireActivity())
    val dialog = builder.setTitle("Permission Required")
        .setMessage("Required permissions have been set to 'Don't ask again'. Please enable them in settings.")
        .setCancelable(true)
        .setNegativeButton("Cancel") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        .setPositiveButton("Settings") { dialogInterface, _ ->
            redirectToSystemSettings(context = context)
            dialogInterface.dismiss()
        }
        .create()

    dialog.setOnShowListener {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            redirectToSystemSettings(context)
            dialog.dismiss()
        }

        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
            dialog.dismiss()
        }
    }

    dialog.show()
}

private fun redirectToSystemSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", context.packageName, null)
    intent.data = uri
    context.startActivity(intent)
}
