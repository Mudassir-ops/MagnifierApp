package com.example.utils

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import etech.magnifierplus.R
import java.io.File
import java.io.FileOutputStream

private var toast: Toast? = null
fun Activity.toast(message: String) {
    try {
        if (this.isDestroyed || this.isFinishing) return
        if (toast != null) {
            toast?.cancel()
        }
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        if (this.isDestroyed || this.isFinishing) return
        toast?.show()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun Activity.shareApp(){
    try {
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.type = "text/plain"
        sendIntent.putExtra(
            Intent.EXTRA_SUBJECT,"ChargingAnimation")
        var shareMessage = "\n Let me recommend you this application\n\n"
        shareMessage = """
             ${shareMessage}https://play.google.com/store/apps/details?id= ${this.packageName}
        """.trimIndent()
        sendIntent.putExtra(Intent.EXTRA_TEXT,shareMessage)
        this.startActivity(Intent.createChooser(sendIntent, "Choose one"))
    }catch (e:java.lang.Exception){
        e.printStackTrace()
        this.toast("No Launcher")
    }
}

fun Activity.feedBackWithEmail(title:String,message:String,emailId:String){
    try {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.flags  = Intent.FLAG_ACTIVITY_CLEAR_TASK
        emailIntent.data  = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailId))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, title)
        emailIntent.putExtra(Intent.EXTRA_TEXT, message)
        this.startActivity(emailIntent)

    }catch (e:java.lang.Exception){
        e.printStackTrace()
    }
}

fun Activity.privacyPolicyUrl(){
    try {
        this.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(this.getString(R.string.privacy_policy_link)))
        )
    }catch (e:Exception){
        e.printStackTrace()
        toast(this.getString(R.string.no_launcher))

    }
}

fun Activity.moreApps(){
    try {
        this.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(this.getString(R.string.more_app_link)))
        )
    }catch (e:Exception){
        e.printStackTrace()
        toast(this.getString(R.string.no_launcher))

    }
}
object ImageSaver {
    fun saveImage(bitmap: Bitmap?, context: Context, onSave: (Uri) -> Unit) {
        val fileName = System.currentTimeMillis().toString() + ".jpg"
        val outputStream = FileOutputStream(File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName))
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        val uri = Uri.fromFile(File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName))
        onSave(uri)
    }
}

private fun saveImageToGallery(bitmap: Bitmap,context: Context) {
    val filename = "IMG_${System.currentTimeMillis()}.jpg"

    // Android 10+ (API 29+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyAppFolder")
        }

        val resolver = context.contentResolver
        val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let {
            val outputStream = resolver.openOutputStream(it)
            outputStream?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.flush()
                Toast.makeText(context, "Image saved in gallery", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // Below Android 10 (API 28 and below)
    else {
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/MyAppFolder"
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
            context,
            arrayOf(imageFile.toString()),
            arrayOf("image/jpeg"),
        ) { _, _ -> }

        Toast.makeText(context, "Image saved in gallery", Toast.LENGTH_SHORT).show()
    }
}

fun Context.feedBackWithEmail(title:String,message:String,emailId:String){
    try {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.flags  = Intent.FLAG_ACTIVITY_CLEAR_TASK
        emailIntent.data  = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailId))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, title)
        emailIntent.putExtra(Intent.EXTRA_TEXT, message)
        this.startActivity(emailIntent)

    }catch (e:java.lang.Exception){
        e.printStackTrace()
    }
}



