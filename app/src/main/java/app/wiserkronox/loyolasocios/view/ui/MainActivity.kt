package app.wiserkronox.loyolasocios.view.ui

import android.content.Intent
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
import app.wiserkronox.loyolasocios.viewmodel.UserViewModel
import app.wiserkronox.loyolasocios.viewmodel.UserViewModelFactory
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
        getGoogleStatus()

    }

    /*************************************************************************************/
    //Funciones de inicio de sesion con Google
    /*************************************************************************************/
    fun getGoogleStatus(){
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_id_token))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)

        if( account == null ) {
            goWithoutSession()
        } else {
            //Check user status for correct redirect
            Log.d("Main", "Check the user status")
            signOutGoogle()
        }
    }

    /*var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        /*if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            doSomeOperations()
        }*/
        if( result.resultCode == RC_SIGN_IN ){
            val task = GoogleSignIn.getSignedInAccountFromIntent( result.data )
            handleSingInResult(task)
        }
    }*/

    fun signInGoogle(){
        goLoader()
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
                verifyGoogleAccount( account )
            } else {
                goFailLogin("No se pudo conectar con la cuenta de Google")
            }

            /*val user = User()
            user.oauth_provider = "google"
            user.oauth_uid = account?.id ?:""
            user.names = account?.givenName?:""
            user.last_name_1 = account?.familyName ?: ""
            user.email = account?.email?:""
            user.picture = account?.photoUrl.toString()

            // Signed in successfully
            val googleId = account?.id ?: ""
            Log.i("Google ID", googleId)

            val googleFirstName = account?.givenName ?: ""
            Log.i("Google First Name", googleFirstName)

            val googleLastName = account?.familyName ?: ""
            Log.i("Google Last Name", googleLastName)

            val googleEmail = account?.email ?: ""
            Log.i("Google Email", googleEmail)

            val googleProfilePicURL = account?.photoUrl.toString()
            Log.i("Google Profile Pic URL", googleProfilePicURL)

            val googleIdToken = account?.idToken ?: ""
            Log.i("Google ID Token", googleIdToken)

            Log.d("Goodle data", account.toString() )
            //Enviar aqui a la actividad de registro*/

        } catch (e: ApiException) {
            Log.e("failed code=", e.statusCode.toString())
            goFailLogin("No se pudo vincular con la cuenta de google: "+e.statusCode.toString())
        }
    }

    fun verifyGoogleAccount(account: GoogleSignInAccount){
        GlobalScope.launch {
            var user_local = LoyolaApplication.getInstance()?.repository?.getUserByOauthUid( account?.id ?:"" )
            if( user_local == null ){
                val user = User()
                user.oauth_provider = "google"
                user.oauth_uid = account?.id ?:""
                user.names = account?.givenName?:""
                user.last_name_1 = account?.familyName ?: ""
                user.email = account?.email?:""
                user.picture = account?.photoUrl.toString()
                registerGoogleUser( user )
            } else {
                //Actualizamos el usuario
                user_local.names = account?.givenName?:""
                user_local.last_name_1 = account?.familyName ?: ""
                user_local.email = account?.email?:""
                user_local.picture = account?.photoUrl.toString()

                if( user_local.state == "")
                    user_local.state = User.REGISTER_LOGIN_STATE

                updateUser( user_local )
            }
        }
    }

    /*fun verifyGoogleUser(user: User){
        //Preguntamos si el usuario no esta ya registrado
        GlobalScope.launch {
            var user_local = LoyolaApplication.getInstance()?.repository?.getUserByOauthUid( user.oauth_uid )
            if( user_local == null ){
                user.state = User.REGISTER_LOGIN_STATE
                registerGoogleUser( user )
            } else {
                //account?.familyName ?: ""
                if( user_local.state == "")
                    user.state = User.REGISTER_LOGIN_STATE
                defineDestination( user )
            }
        }
    }*/

    fun registerGoogleUser(user: User){
        GlobalScope.launch {
            val id = LoyolaApplication.getInstance()?.repository?.insert(user)
            if (id != null) {
                if( id > 0 ) {
                    Log.d(TAG, "Id usuario nuevo "+ id )
                    var user = LoyolaApplication.getInstance()?.repository?.getUserByOauthUid(user.oauth_uid)
                    user?.let{
                        defineDestination( user )
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
                goWithoutSession()
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
                Log.d(MainActivity.TAG, "user"+user.password)
                defineDestination( user )
            } else {
                Log.d(MainActivity.TAG, "no hay usuario")
                goFailLogin("No se encontro el usuario")
            }


            /*LoyolaApplication.getInstance()?.repository?.getUserEmail(email).apply { users ->
                if( users != null ) {
                    Log.d(MainActivity.TAG, "ususaio"+users.count())
                    users.collect {
                        Log.d(MainActivity.TAG, "user"+it.password)
                        defineDestination( it )
                    }
                } else {
                    Log.d(MainActivity.TAG, "no hay usuario")
                    goWithoutSession();
                    //Toast.makeText(acti, "No se encontro el usuario", Toast.LENGTH_SHORT).show()
                }
            }*/

            /*if( users != null ) {
                Log.d(MainActivity.TAG, "ususaio"+users.count())
                users.collect {
                    Log.d(MainActivity.TAG, "user"+it.password)
                    defineDestination( it )
                }
            } else {
                Log.d(MainActivity.TAG, "no hay usuario")
                goWithoutSession();
                //Toast.makeText(acti, "No se encontro el usuario", Toast.LENGTH_SHORT).show()
            }*/
        }


        /*if( user == null ){
            goWithoutSession();
            Toast.makeText(this, "No se encontro el usuario", Toast.LENGTH_SHORT).show()
        } else {
            defineDestination(user as User)
        }*/

        /*userViewModel.getUserByEmail(email).observe(this){ user ->
            Log.d(MainActivity.TAG, "Respuesta de la BD ")
            if( user == null ){
                goWithoutSession();
                Toast.makeText(this, "No se encontro el usuario", Toast.LENGTH_SHORT).show()
            } else {
                defineDestination(user)
            }
        }*/
    }

    /*fun getUserByOauthUid(oauthId: String) {
        goLoader()
        GlobalScope.launch {
            val user = LoyolaApplication.getInstance()?.repository?.getUserByOauthUid(oauthId)
            if (user != null) {
                Log.d(MainActivity.TAG, "user" + user.password)
                defineDestination(user)
            } else {
                Log.d(MainActivity.TAG, "no hay usuario")
                goFailLogin("No se encontro el usuario")
            }
        }
    }*/

    fun defineDestination(user: User){
        LoyolaApplication.getInstance()?.user = user
        when( user.state ){
            User.REGISTER_LOGIN_STATE -> goRegisterData(user)
            User.REGISTER_DATA_STATE -> goRegisterPictures(user)
            User.UNREVISED_STATE -> goHomeWithEmail(user.email)
            else -> goWithoutSession()
        }
    }

    /*************************************************************************************/
    //Funciones para ir al fragmento de interes u otra actividad
    /*************************************************************************************/

    fun goManualRegister( ){
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<ManualRegisterFragment>(R.id.fragment_container_view)
        }
    }

    fun goWithoutSession( ){
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

    // Funcion temporal del demo
    fun  goListUsers(){
        val intent = Intent(this@MainActivity, ListUserActivity::class.java)
        startActivity(intent)
    }

    fun goFailLogin(message: String){
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

    fun takePicture( type: Int ){
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra(CameraActivity.REQUEST_TYPE, type)
        startActivityForResult(intent, REQUEST_FOR_PHOTO)
    }

    fun showMessage(message: String){
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    /***************************************************************************************/
    // Funciones para el registro de usuarios
    /***************************************************************************************/

    /*fun verifyGoogleUser(user: User){
        //Preguntamos si el usuario no esta ya registrado
        GlobalScope.launch {
            var user_local = LoyolaApplication.getInstance()?.repository?.getUserByOauthUid( user.oauth_uid )
            if( user_local == null ){
                user.state = User.REGISTER_LOGIN_STATE
                registerGoogleUser( user )
            } else {
                //account?.familyName ?: ""
                if( user_local.state == "")
                    user.state = User.REGISTER_LOGIN_STATE
                defineDestination( user )
            }
        }
    }*/

    fun registerManualUser(user: User){

        /*userViewModel.insert2(user).observe(this){
            Log.d(TAG, "Ya mande a registrar")
            if( it > 0 ){
                getUserByEmailPassword(user.email, user.password)
            }  else {
                goFailLogin("NO se pudo insertar el usuario ")
            }
        }*/


        GlobalScope.launch {
            val id = LoyolaApplication.getInstance()?.repository?.insert(user)
            if (id != null) {
                if( id > 0 ) {
                    Log.d(TAG, "Id usuario nuevo "+ id )
                    getUserByEmailPassword(user.email, user.password)
                } else {
                    goFailLogin("NO se pudo insertar el usuario ")
                }
            }
        }

        //}

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

}