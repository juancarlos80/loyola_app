package app.wiserkronox.loyolasocios.view.ui

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import app.wiserkronox.loyolasocios.R
import app.wiserkronox.loyolasocios.lifecycle.MainObserver
import app.wiserkronox.loyolasocios.service.LoyolaApplication
import app.wiserkronox.loyolasocios.service.model.User
import app.wiserkronox.loyolasocios.service.repository.FileDataPart
import app.wiserkronox.loyolasocios.service.repository.LoyolaService
import app.wiserkronox.loyolasocios.service.repository.UserRest
import app.wiserkronox.loyolasocios.service.repository.VolleyMultipartRequest
import app.wiserkronox.loyolasocios.viewmodel.UserViewModel
import app.wiserkronox.loyolasocios.viewmodel.UserViewModelFactory
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    companion object {
        private const val TAG = "MainActivity"
        private val REQUEST_FOR_PHOTO = 101
    }

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

        lifecycle.addObserver(MainObserver())

        //Primero se verifica si no esta registrado con google
        //getGoogleStatus()
        //getUserFromServerByEmailPassord2("hola", "dos");

    }

    override fun onResume() {
        super.onResume()

        Log.d(TAG, "onResume")
        //Verificamos que no tenga informacion guardada para el inicio de sesion
        val sharedPref = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE) ?: return
        val email = sharedPref.getString("email", "")?:""
        val password = sharedPref.getString("password", "")?:""

        if( !email.equals("") && !password.equals("")){
            getUserByEmailPassword(email, password)
            return
        }

        val oauth_uid = sharedPref.getString("oauth_uid", "")?:""
        if( !oauth_uid.equals("")){
            getGoogleStatus()
            return
        }
        goWithoutSession()
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
                user.oauth_uid = account?.id ?:""
                user.names = account?.givenName?:""
                user.last_name_1 = account?.familyName ?: ""
                user.email = account?.email?:""
                user.picture = account?.photoUrl.toString()
                registerGoogleUser(user)
            } else {
                //Actualizamos el usuario
                user_local.names = account?.givenName?:""
                user_local.last_name_1 = account?.familyName ?: ""
                user_local.email = account?.email?:""
                user_local.picture = account?.photoUrl.toString()

                if( user_local.state == "")
                    user_local.state = User.REGISTER_LOGIN_STATE

                updateUser(user_local)
            }
        }
    }

    fun registerGoogleUser(user: User){
        GlobalScope.launch {
            val id = LoyolaApplication.getInstance()?.repository?.insert(user)
            if (id != null) {
                if( id > 0 ) {
                    Log.d(TAG, "Id usuario nuevo " + id)
                    var user = LoyolaApplication.getInstance()?.repository?.getUserByOauthUid(user.oauth_uid)
                    user?.let{
                        defineDestination(user)
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
            val user = LoyolaApplication.getInstance()?.repository?.getUserEmail(email)
            if( user != null ) {
                Log.d(MainActivity.TAG, "user" + user.password)
                saveManualUserLogin(user.email, user.password)
                defineDestination(user)
            } else {
                Log.d(MainActivity.TAG, "no hay usuario")
                goFailLogin("No se encontro el usuario")
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
            User.UNREVISED_STATE -> goHomeWithEmail(user.email)
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
        Log.d(TAG, "Ir al fragmento de mis datos")
        val fragment = MyDataFragment.newInstance(cUser)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container_view, fragment)
        //transaction.addToBackStack(null)
        transaction.setReorderingAllowed(true)
        transaction.commit()

        /*supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<fragment>(R.id.fragment_container_view)
        }*/
    }

    fun goRegisterPictures(cUser: User){
        val fragment = PicturesFragment.newInstance(cUser)
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

    // Funcion temporal del demo
    fun  goListUsers(){
        val intent = Intent(this@MainActivity, ListUserActivity::class.java)
        startActivity(intent)
    }

    fun goFailLogin(message: String){
        removeDataUser()
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            goWithoutSession()
        }
    }

    fun goHomeWithEmail(email: String){
        val intent = Intent(this@MainActivity, HomeActivity::class.java)
        intent.putExtra("user_email", email)
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
    }




    /**********************************************************************************************/
    //Operaciones de Red
    /**********************************************************************************************/
    fun isOnline(): Boolean {
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connMgr.activeNetworkInfo
        return networkInfo?.isConnected == true
    }

    fun getUserFromServerByEmailPassord(email: String, password: String){
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val url = "http://www.google.com"

        // Request a string response from the provided URL.
        val stringRequest = StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    // Display the first 500 characters of the response string.
                    Log.d(TAG, "Response is: ${response.substring(0, 500)}")
                },
                Response.ErrorListener {
                    Log.d(TAG, "That didn't work!")
                })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)

    }

    fun setUserServer(user: User){
        // Instantiate the RequestQueue.
        goLoader()
        val userRest = UserRest(this)

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, userRest.getUserDataURL(), userRest.getUserDataJson(user),
                Response.Listener { response ->
                    Log.d(TAG, "Response is: ${response.toString()}")
                    if (response.getBoolean("success")) {
                        Log.d(TAG, "Exito")
                        showMessage("Primer paso guardado")
                    } else {
                        showMessage(response.getString("reason"))
                        goTerms(user)
                    }
                },
                Response.ErrorListener { error ->
                    // TODO: Handle error
                    Log.e(TAG, error.toString())
                    error.printStackTrace()
                    showMessage("Error de conexión con el servidor")
                    goTerms(user)
                }
        )

        // Add the request to the RequestQueue.
        LoyolaService.getInstance(this).addToRequestQueue(jsonObjectRequest)

    }
    //private var imageData: ByteArray? = null

    private fun uploadImageServer(type_photo: String, imageName:String, imageData: ByteArray,
                                  type_auth: String, value_auth: String) {
        val postURL =  UserRest(this).getUserPictureURL()
        //imageData?: return
        val request = object : VolleyMultipartRequest(
                Request.Method.POST,
                postURL,
                Response.Listener {
                    var resp = String(it.data)
                    Log.d("Upload", "response is: $resp")
                },
                Response.ErrorListener {
                    Log.d("Upload", "error is: $it")
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
        Volley.newRequestQueue(this).add(request)
    }

    //@Throws(IOException::class)
    fun uploadImageUriEmail(type_photo: String, uri: String,
                            type_auth: String, value_auth: String) {
        Log.d("Upload", uri)
        val photo = File( Uri.parse(uri).path )
        val inputStream = contentResolver.openInputStream(Uri.parse(uri))
        inputStream?.buffered()?.use {
            uploadImageServer(type_photo, photo.name, it.readBytes(), type_auth, value_auth)
        }
    }

}