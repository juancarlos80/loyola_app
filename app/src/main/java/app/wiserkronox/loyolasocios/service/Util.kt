package app.wiserkronox.loyolasocios.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import app.wiserkronox.loyolasocios.R
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

class Util {
    companion object{
        fun saveImage(image: Bitmap, filename: String): File? {
            val imageFile = File(filename)
            val os = BufferedOutputStream(FileOutputStream(imageFile))
            image.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.close()
            return imageFile
        }

        fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(degrees)
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        fun rotateImageFile(imagePath: String, degrees: Int){
            var bitmap = BitmapFactory.decodeFile(imagePath)
            bitmap = rotate(bitmap, degrees.toFloat())
            saveImage( bitmap, imagePath )
        }

        fun deleteIfExists(uriFile: String){
            val image = File(Uri.parse(uriFile)?.path)
            if( image.exists() ) image.delete()
        }

        fun getOutputDirectory(activity: AppCompatActivity): File {
            val mediaDir = activity.externalMediaDirs.firstOrNull()?.let {
                File(it, activity.resources.getString(R.string.app_name)
                        + activity.resources.getString(R.string.photo_directory)).apply { mkdirs() } }

            return if (mediaDir != null && mediaDir.exists())
                mediaDir else activity.filesDir
        }

    }
}