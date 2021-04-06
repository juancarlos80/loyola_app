package app.wiserkronox.loyolasocios.view.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.impl.utils.Exif
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import app.wiserkronox.loyolasocios.R
import app.wiserkronox.loyolasocios.service.LoyolaApplication
import app.wiserkronox.loyolasocios.service.model.User
import app.wiserkronox.loyolasocios.viewmodel.UserViewModel
import app.wiserkronox.loyolasocios.viewmodel.UserViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraActivity : AppCompatActivity() {

    //Textos de la actividad
    private lateinit var text_title: TextView
    //private lateinit var text_subtitle: TextView

    //Para la activacion de la camara
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var viewFinder: PreviewView
    private var back_camera = true;
    private var current_request = REQUEST_PICTURE_1;

    companion object {
        private const val TAG = "Camera Activity"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

        const val REQUEST_TYPE = "request_type"
        const val REQUEST_PICTURE_1 = 1
        const val REQUEST_PICTURE_2 = 2
        const val REQUEST_SELFIE = 3
    }

    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory((application as LoyolaApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        viewFinder = findViewById(R.id.viewFinder)
        text_title = findViewById(R.id.text_title_photo)
        //text_title.text = "Probando un textito"

        intent.getIntExtra(REQUEST_TYPE, 0).let {
            current_request = it
            when ( it ){
                REQUEST_PICTURE_1 -> {
                    supportActionBar?.title = "Foto del Anverso CI"
                    text_title.text = "La informacion debe leerse claramente"
                }
                REQUEST_PICTURE_2 -> {
                    supportActionBar?.title = "Foto del Reverso CI"
                    text_title.text = "La informacion debe leerse claramente"
                }
                REQUEST_SELFIE -> {
                    supportActionBar?.title = "AutoFoto Seflie"
                    text_title.text = resources.getString(R.string.text_selfie_hint)
                    back_camera = false
                }
                else ->{
                    supportActionBar?.title = "No se reconoce la accion"
                }
            }

        }

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }


        // Set up the listener for take photo button
        val btnPhoto = findViewById<FloatingActionButton>(R.id.fbtn_take_photo)
        LoyolaApplication.getInstance()?.user?.let { user ->
            btnPhoto.setOnClickListener {
                takePhoto(user)
            }
        }

        outputDirectory = getOutputDirectory()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    /*************************************************************************************/
    // Funciones para la toma de fotografias
    /*************************************************************************************/

    fun takePhoto(user: User) {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
                outputDirectory,
                SimpleDateFormat(FILENAME_FORMAT, Locale.US
                ).format(System.currentTimeMillis()) + ".jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
            .build()

        //imageCapture.targetRotation = viewFinder.display.rotation

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
                outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e(CameraActivity.TAG, "No se pudo tomar la foto: ${exc.message}", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                //Log.d(CameraActivity.TAG, "url: " + savedUri.path)

                val exif = Exif.createFromFile(photoFile)
                val rotation = exif.rotation
                //Log.d(TAG, "rotacion: " + rotation)
                if( rotation > 0 ){
                    rotateImageFile(photoFile.path, rotation)
                } else {
                    var bitmap = BitmapFactory.decodeFile(photoFile.path)
                    saveImage( bitmap, photoFile.path)
                }
                //val msg = "La foto se guardo: $savedUri"
                //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                //Log.d(CameraActivity.TAG, msg)
                when (current_request) {
                    REQUEST_PICTURE_1 -> {
                        if (!user.picture_1.equals("")) {
                            deleteIfExists(user.picture_1)
                        }
                        user.picture_1 = savedUri.toString()
                    }
                    REQUEST_PICTURE_2 -> {
                        if (!user.picture_2.equals("")) {
                            deleteIfExists(user.picture_2)
                        }
                        user.picture_2 = savedUri.toString()
                    }
                    REQUEST_SELFIE -> {
                        if (!user.selfie.equals("")) {
                            deleteIfExists(user.selfie)
                        }
                        user.selfie = savedUri.toString()
                    }
                }
                userViewModel.update(user)
                finish()
            }
        })

    }

    private fun startCamera( ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(viewFinder.surfaceProvider)
                    }

            imageCapture = ImageCapture.Builder()
                    .setMaxResolution(Size(480, 640))
                    .build()

            // Select back camera as a default
            val cameraSelector =
                    if (back_camera) CameraSelector.DEFAULT_BACK_CAMERA
                    else CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(CameraActivity.TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }


    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)
                    + resources.getString(R.string.photo_directory)).apply { mkdirs() } }

        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults:
            IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                        this,
                        "Debes otorgar el permiso para poder subir las fotos",
                        Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    fun deleteIfExists(uriFile: String){
        val image = File(Uri.parse(uriFile)?.path)
        if( image.exists() ) image.delete()
    }

    private fun rotateImageFile(imagePath: String, degrees: Int){
        var bitmap = BitmapFactory.decodeFile(imagePath)
        bitmap = rotate(bitmap, degrees.toFloat())
        saveImage( bitmap, imagePath )
    }

    private fun saveImage(image: Bitmap, filename: String): File? {
        val imageFile = File(filename)
        val os = BufferedOutputStream(FileOutputStream(imageFile))
        image.compress(Bitmap.CompressFormat.JPEG, 100, os)
        os.close()
        return imageFile
    }

    private fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}