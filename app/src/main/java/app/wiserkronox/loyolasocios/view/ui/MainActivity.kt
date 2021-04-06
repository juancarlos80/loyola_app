package app.wiserkronox.loyolasocios.view.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import app.wiserkronox.loyolasocios.R
import app.wiserkronox.loyolasocios.lifecycle.MainObserver
import app.wiserkronox.loyolasocios.service.LoyolaApplication
import app.wiserkronox.loyolasocios.service.Util
import app.wiserkronox.loyolasocios.service.model.User
import app.wiserkronox.loyolasocios.service.repository.FileDataPart
import app.wiserkronox.loyolasocios.service.repository.LoyolaService
import app.wiserkronox.loyolasocios.service.repository.UserRest
import app.wiserkronox.loyolasocios.service.repository.VolleyMultipartRequest
import app.wiserkronox.loyolasocios.viewmodel.UserViewModel
import app.wiserkronox.loyolasocios.viewmodel.UserViewModelFactory
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    private var google_request = false

    private var updateDataUser = false
    lateinit var requestUpload: RequestQueue

    companion object {
        private const val TAG = "MainActivity"
        private val REQUEST_FOR_PHOTO = 101
        val FLAG_UPDATE_USER_DATA = "update_data"
    }

    fun isUpdateDataUser (): Boolean {
        return updateDataUser
    }

    lateinit var uploadDataFragment: UploadDataFragment

    private val userViewModel: UserViewModel by viewModels {
        UserViewModelFactory((application as LoyolaApplication).repository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        if( savedInstanceState == null ){
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<LoadingFragment>(R.id.fragment_container_view)
            }
        }

        updateDataUser = intent.getBooleanExtra(FLAG_UPDATE_USER_DATA, false)

        lifecycle.addObserver(MainObserver())

        //Primero se verifica si no esta registrado con google
        //getGoogleStatus()
        //getUserFromServerByEmailPassord2("hola", "dos");

    }

    override fun onResume() {
        super.onResume()

        //Si el usario ya esta en sesion entonces no controlamos shared data
        val user = LoyolaApplication.getInstance()?.user
        if( user != null ){
            defineDestination(user)
            return
        }

        Log.d(TAG, "onResume")
        //Verificamos que no tenga informacion guardada para el inicio de sesion
        val sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE) ?: return
        val email = sharedPref.getString("email", "")?:""
        val password = sharedPref.getString("password", "")?:""

        val oauth_uid = sharedPref.getString("oauth_uid", "")?:""
        if( !oauth_uid.equals("")){
            getGoogleStatus()
            return
        }

        if( !email.equals("") && !password.equals("")){
            getUserByEmailPassword(email, password)
            return
        }

        if( !google_request ) {
            goWithoutSession()
        }
    }

    /*************************************************************************************/
    //Funciones de inicio de sesion con Google
    /*************************************************************************************/
    fun getGoogleStatus(){
        if ( !this::mGoogleSignInClient.isInitialized ) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.google_id_token))
                    .requestEmail()
                    .build()

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        }

        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)

        if( account == null ) {
            goWithoutSession()
        } else {
            verifyGoogleAccount(account)
        }
    }

    fun signInGoogle(){
        goLoader()
        if ( !this::mGoogleSignInClient.isInitialized ) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.google_id_token))
                    .requestEmail()
                    .build()

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        }
        val singInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(singInIntent, RC_SIGN_IN)
        //resultLauncher.launch( singInIntent )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if( requestCode == RC_SIGN_IN ){
            google_request = true
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSingInResult(task)
        }
    }

    private fun handleSingInResult(completedTask: Task<GoogleSignInAccount>){
        try {
            val account = completedTask.getResult(
                    ApiException::class.java
            )

            if( account != null ){
                verifyGoogleAccount(account)
            } else {
                goFailLogin("No se pudo conectar con la cuenta de Google")
            }

        } catch (e: ApiException) {
            Log.e("failed code=", e.statusCode.toString())
            goFailLogin("No se pudo vincular con la cuenta de google: " + e.statusCode.toString())
        }
    }

    fun verifyGoogleAccount(account: GoogleSignInAccount){
        saveOauthlUserLogin(account?.id ?: "")
        GlobalScope.launch {
            var user_local = LoyolaApplication.getInstance()?.repository?.getUserByOauthUid(account?.id
                    ?: "")
            if( user_local == null ){
                val user = User()
                user.oauth_provider = "google"
                user.oauth_uid = account.id ?:""
                user.names = account.givenName?:""
                user.last_name_1 = account.familyName ?: ""
                user.email = account.email?:""
                user.picture = account.photoUrl.toString()
                registerGoogleUser(user)
            } else {
                if( user_local.state == "") {
                    //Actualizamos el usuario si es primera vez
                    user_local.names = account.givenName?:""
                    user_local.last_name_1 = account.familyName ?: ""
                    user_local.email = account.email?:""
                    user_local.picture = account.photoUrl.toString()
                    user_local.state = User.REGISTER_LOGIN_STATE
                    updateUser(user_local)
                } else {
                    defineDestination( user_local )
                }
            }
        }
    }

    fun registerGoogleUser(user: User){
        GlobalScope.launch {
            val id = LoyolaApplication.getInstance()?.repository?.insert(user)
            if (id != null) {
                if( id > 0 ) {
                    Log.d(TAG, "Id usuario nuevo " + id)
                    var user_db = LoyolaApplication.getInstance()?.repository?.getUserByOauthUid(user.oauth_uid)
                    user_db?.let{
                        defineDestination(it)
                    }
                } else {
                    goFailLogin("NO se pudo insertar el usuario ")
                }
            }
        }
    }

    private fun signOutGoogle() {
        mGoogleSignInClient.signOut()
            .addOnCompleteListener(this) {
                Log.d(TAG, "Ya quite")
            }
    }

    private fun revokeAccess() {
        mGoogleSignInClient.revokeAccess()
            .addOnCompleteListener(this) {
                // ...
            }
    }

    /*************************************************************************************/
    //Funciones para el inicio de sesion manual
    /*************************************************************************************/
    fun getUserByEmailPassword(email: String, password: String){
        goLoader()
        GlobalScope.launch {
            val user = LoyolaApplication.getInstance()?.repository?.getUserEmailPassword(email, password)
            if( user != null ) {
                //Log.d(MainActivity.TAG, "user" + user.password)
                saveManualUserLogin(user.email, user.password)
                defineDestination(user)
            } else {
                if( isOnline() ) {
                    Log.d(MainActivity.TAG, "no hay usuario buscando en el servidor")
                    getUserFromServer(email, password, "")
                } else {
                 showMessage("Si ya esta registrado, debe estar conectado a Internet para buscar su información")
                }
            }
        }
    }

    fun updateByDataJSON(email: String, oauth_uid: String, userServer: User) {
        goLoader()
        GlobalScope.launch {
            val userLocal: User?
            if( oauth_uid != "" ) {
                userLocal = LoyolaApplication.getInstance()?.repository?.getUserByOauthUid(oauth_uid)
            } else {
                userLocal = LoyolaApplication.getInstance()?.repository?.getUserByEmail(email)
            }

            //val userServer = userRest.getUserFromJSON( response.getJSONObject("user"))

            if( userServer == null){
                showMessage("No se pudo descargar los datos del socio del servidor")
            } else {
                if (userLocal == null) {
                    //Insertar el nuevo usuario del servidor
                    registerManualUser( userServer )
                }
            }
        }
    }


    /*************************************************************************************/
    //Funciones para ir al fragmento de interes u otra actividad
    /*************************************************************************************/
    fun defineDestination(user: User){
        LoyolaApplication.getInstance()?.user = user
        when( user.state ){
            User.REGISTER_LOGIN_STATE -> goRegisterData(user)
            User.REGISTER_DATA_STATE -> goRegisterPictures(user)
            User.REGISTER_PICTURE_STATE -> goTerms(user)
            User.UPLOAD_DATA_SERVER -> goUploadData(user)
            User.COMPLETE_STATE -> goHome()
            User.DOWNLOAD_DATA_SERVER -> downloadPictures(user)
            else -> goWithoutSession()
        }
    }

    fun goManualRegister( ){
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<ManualRegisterFragment>(R.id.fragment_container_view)
        }
    }

    fun goWithoutSession( ){
        Log.d(TAG, "ir Sin Sesion")
        removeDataUser()
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<WithoutSessionFragment>(R.id.fragment_container_view)
        }
    }

    fun goLoader( ){
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<LoadingFragment>(R.id.fragment_container_view)
        }
    }

    fun goRegisterData(cUser: User){
        val fragment = MyDataRegisterFragment.newInstance(cUser)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container_view, fragment)
        //transaction.addToBackStack(null)
        transaction.setReorderingAllowed(true)
        transaction.commit()
    }

    fun goRegisterPictures(cUser: User){
        val fragment = PicturesRegisterFragment.newInstance(cUser)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container_view, fragment)
        transaction.setReorderingAllowed(true)
        transaction.commit()
    }

    fun goTerms(cUser: User){
        val fragment = TermsFragment.newInstance(cUser)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container_view, fragment)
        transaction.setReorderingAllowed(true)
        transaction.commit()
    }

    fun goUploadData(cUser: User){
        uploadDataFragment = UploadDataFragment.newInstance(cUser)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container_view, uploadDataFragment)
        transaction.setReorderingAllowed(true)
        transaction.commit()
    }

    fun goFailLogin(message: String){
        removeDataUser()
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            goWithoutSession()
        }
    }

    fun goHome(){
        val intent = Intent(this@MainActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun takePicture(type: Int){
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra(CameraActivity.REQUEST_TYPE, type)
        startActivityForResult(intent, REQUEST_FOR_PHOTO)
    }

    fun showMessage(message: String){
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    fun goThanks( ){
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<ThanksFragment>(R.id.fragment_container_view)
        }
    }

    /***************************************************************************************/
    // Funciones para el registro de usuarios
    /***************************************************************************************/

    fun registerManualUser(user: User){
        goLoader()
        GlobalScope.launch {
            val id = LoyolaApplication.getInstance()?.repository?.insert(user)
            if (id != null) {
                if( id > 0 ) {
                    Log.d(TAG, "Id usuario nuevo " + id)
                    getUserByEmailPassword(user.email, user.password)
                } else {
                    goFailLogin("NO se pudo insertar el usuario ")
                }
            }
        }
    }

    fun updateUser(user: User){
        goLoader()
        GlobalScope.launch {
            val id = LoyolaApplication.getInstance()?.repository?.update2(user)?:0
            if (id > 0) {
                defineDestination(user)
            } else {
                showMessage("No se pudo actualizar la informacion del usuario")
            }
        }
    }

    fun backUpdate(user: User){
        GlobalScope.launch {
            LoyolaApplication.getInstance()?.repository?.update2(user)
            LoyolaApplication.getInstance()?.user = user
        }
    }

    fun saveManualUserLogin(email: String, password: String){
        val sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)?: return
        with(sharedPreferences.edit()){
            putString("email", email)
            putString("password", password)

            remove("oauth_uid")
            commit()
        }
    }

    fun saveOauthlUserLogin(oauth_uid: String){
        val sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)?: return
        with(sharedPreferences.edit()){
            putString("oauth_uid", oauth_uid)

            remove("email")
            remove("password")
            commit()
        }
    }

    fun removeDataUser(){
        Log.d(TAG, "Quito datos usuario")
        if ( this::mGoogleSignInClient.isInitialized ){
            signOutGoogle()
        }

        val sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)?: return
        with(sharedPreferences.edit()){
            remove("oauth_uid")
            remove("email")
            remove("password")
            commit()
        }

        LoyolaApplication.getInstance()?.user = null
    }

    /**********************************************************************************************/
    //Operaciones de Red
    /**********************************************************************************************/
    fun isOnline(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connMgr.activeNetworkInfo
        return networkInfo?.isConnected == true
    }


    fun getUserFromServer(email: String, password: String, oauth_uid: String){
        val userRest = UserRest(this)
        val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST, userRest.getUserLoginURL(),
                userRest.getUserDataLoginJson( email, password, oauth_uid),
                { response ->
                    Log.d(TAG, "Response is: $response")
                    if (response.getBoolean("success")) {
                        Log.d(TAG, "Exito")
                        userRest.getUserFromJSON( response.getJSONObject("user"))?.let {
                            updateByDataJSON(email, oauth_uid, it )
                        }?: run {
                            showMessage("No se pudo descargar los datos del socio del servidor")
                        }
                    } else {
                        Log.e(TAG, "No se encontro en la red")
                        showMessage("No se encontro el usuario")
                        goWithoutSession()
                    }
                },
                { error ->
                    Log.e(TAG, error.toString())
                    Log.e(TAG, userRest.getUserLoginURL())
                    Log.e(TAG, error.message.toString())
                    error.printStackTrace()
                    showMessage("Error de conexión con el servidor")
                }
        )

        // Add the request to the RequestQueue.
        LoyolaService.getInstance(this).addToRequestQueue(jsonObjectRequest)

    }

    fun uploadUserDataServer(user: User){
        val userRest = UserRest(this)
        val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST, userRest.getUserDataURL(),
                userRest.getUserDataJson(user, updateDataUser),
                { response ->

                    Log.d(TAG, "Response is: $response")
                    if (response.getBoolean("success")) {
                        Log.d(TAG, "Exito")

                        user.data_online = true;
                        backUpdate( user )
                        uploadDataFragment.sendUserImages( user )
                    } else {
                        showMessage(response.getString("reason"))
                    }
                    uploadDataFragment.terminateProgress("data", response.getBoolean("success"))

                },
                { error ->
                    Log.e(TAG, error.toString())
                    Log.e(TAG, userRest.getUserDataURL())
                    Log.e(TAG, error.message.toString())
                    error.printStackTrace()
                    showMessage("Error de conexión con el servidor")
                    uploadDataFragment.terminateProgress("data", false)
                }
        )

        // Add the request to the RequestQueue.
        LoyolaService.getInstance(this).addToRequestQueue(jsonObjectRequest)

    }

    private fun uploadImageServer(type_photo: String, imageName:String, imageData: ByteArray,
                                  type_auth: String, value_auth: String, user: User) {
        val postURL =  UserRest(this).getUserPictureURL()
        //imageData?: return
        val request = object : VolleyMultipartRequest(
                Request.Method.POST,
                postURL,
                Response.Listener {
                    Log.d("Upload", " $type_photo response is: "+String(it.data))
                    try {
                        val j_response = JSONObject(String(it.data))
                        uploadDataFragment?.terminateProgress( type_photo, j_response.getBoolean("success"))
                        if(!j_response.getBoolean("success")){
                            showMessage("No se subir la foto al servidor "+j_response.getString("reason"))
                        } else {
                            //Si se guardo exitosamente, actualizamos y preguntamos por la siguiente imagen
                            if( type_photo == UploadDataFragment.UPLOAD_TYPE_PICTURE_1 ){
                                user.picture_1_online = true
                                backUpdate( user )
                                uploadDataFragment.sendUserImages( user )
                            }

                            if( type_photo == UploadDataFragment.UPLOAD_TYPE_PICTURE_2 ){
                                user.picture_2_online = true
                                backUpdate( user )
                                uploadDataFragment.sendUserImages( user )
                            }

                            if( type_photo == UploadDataFragment.UPLOAD_TYPE_SELFIE ){
                                user.selfie_online = true
                                user.state = User.COMPLETE_STATE
                                user.state_activation = User.STATE_USER_INACTIVE
                                backUpdate( user )
                                goThanks()
                            }

                        }
                    }catch ( j_error: JSONException ){
                        j_error.printStackTrace()
                        showMessage("No se pudo leer la respuesta del servidor")
                    }

                },
                Response.ErrorListener {
                    Log.d("Upload", "error is: $it")
                    showMessage("No se pudo conectar al servidor")
                }
        ) {
            override fun getByteData(): MutableMap<String, FileDataPart> {
                var params = HashMap<String, FileDataPart>()
                params["imageFile"] = FileDataPart(imageName, imageData!!, "jpeg")
                return params
            }

            //@Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String>? {
                val params: MutableMap<String, String> = HashMap()
                params[type_auth] = value_auth
                params["type_photo"] = type_photo
                return params
            }
        }
        requestUpload = Volley.newRequestQueue(this)
        requestUpload.add(request)
    }

    //@Throws(IOException::class)
    fun uploadUriServer(type_photo: String, uri: String,
                            type_auth: String, value_auth: String, user: User) {
        Log.d("Upload", uri)
        val photo = File( Uri.parse(uri).path )
        val inputStream = contentResolver.openInputStream(Uri.parse(uri))
        inputStream?.buffered()?.use {
            uploadImageServer(type_photo, photo.name, it.readBytes(), type_auth, value_auth, user)
        }
    }

    fun cancelRequests(){
        if(!this::requestUpload.isInitialized ) {
            requestUpload.cancelAll(this)
        }
        removeDataUser()
        goWithoutSession()
    }


    fun downloadPictures(user: User){
        if( user.picture_1.startsWith("http") ){
            downloadPicture("picture_1", user.picture_1, user)
            return
        }
        if( user.picture_2.startsWith("http") ){
            downloadPicture("picture_2", user.picture_2, user)
            return
        }
        if( user.selfie.startsWith("http") ){
            downloadPicture("selfie", user.selfie, user)
        }
        user.state = User.COMPLETE_STATE;
        updateUser( user )
    }

    fun downloadPicture(type_photo: String, url_picture: String, user: User){
        val imageRequest = ImageRequest(
                url_picture,
                {bitmap -> // response listener
                    Log.d(TAG, "Descargando Imagen")
                    Log.d(TAG, "URL: "+url_picture)
                    Log.d(TAG, "filename: "+url_picture.substring( url_picture.lastIndexOf("/")+1))

                    val photo = Util.saveImage( bitmap, Util.getOutputDirectory(this).toString()+url_picture.substring( url_picture.lastIndexOf("/")+1));
                    photo?.let{
                        if( type_photo == "picture_1") {
                            user.picture_1 = Uri.fromFile(it).toString()
                            backUpdate( user )
                            downloadPictures( user )
                        }
                        if( type_photo == "picture_2") {
                            user.picture_2 = Uri.fromFile(it).toString()
                            backUpdate( user )
                            downloadPictures( user )
                        }
                        if( type_photo == "selfie") {
                            user.selfie = Uri.fromFile(it).toString()
                            backUpdate( user )
                            downloadPictures( user )
                        }
                    }
                },
                0, // max width
                0, // max height
                ImageView.ScaleType.CENTER_CROP, // image scale type
                Bitmap.Config.ARGB_8888, // decode config
                {error-> // error listener
                    Log.d(TAG, error.message.toString())
                    removeDataUser()
                    goWithoutSession()
                }
        )

        LoyolaService.getInstance(this).addToRequestQueue(imageRequest)
    }

}