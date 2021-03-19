package app.wiserkronox.loyolasocios.view.ui

import android.content.Intent
import android.os.Bundle
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


class MainActivity : AppCompatActivity() {

    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

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

    fun signInGoogle(){
        val singInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(singInIntent, RC_SIGN_IN)
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

            //Enviar aqui a la actividad de registro

        } catch (e: ApiException) {
            // Sign in was unsuccessful
            Log.e(
                "failed code=", e.statusCode.toString()
            )
            goWithoutSession()
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
    fun getUserByEmailPassword( email: String, password :String ){
        goLoader()
        userViewModel.getUserByEmail(email).observe(this){ user ->
            if( user == null ){
                goWithoutSession();
                Toast.makeText(this, "No se encontro el usuario", Toast.LENGTH_SHORT).show()
            } else {
                defineDestination( user )
            }
        }
    }

    fun defineDestination( user: User){
        when( user.state ){
            User.REGISTER_LOGIN_STATE -> goRegisterData( user )
            //User.REGISTER_DATA_STATE -> goRegisterData( user )
            User.DATA_COMPLETE_STATE -> goHomeWithEmail( user.email )
            else -> goRegisterData( user )
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

    fun goRegisterData( cUser: User){
        val fragment = MyDataFragment.newInstance(cUser)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container_view,fragment)
        //transaction.addToBackStack(null)
        transaction.setReorderingAllowed(true)
        transaction.commit()

        /*supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<fragment>(R.id.fragment_container_view)
        }*/
    }

    // Funcion temporal del demo
    fun  goListUsers(){
        val intent = Intent(this@MainActivity, ListUserActivity::class.java)
        startActivity(intent)
    }

    /***************************************************************************************/
    // Funciones para el registro de usuarios
    /***************************************************************************************/

    fun registerManualUser( user: User){
        userViewModel.insert( user )
        defineDestination( user )
    }

    fun updateUser( user: User){
        userViewModel.update( user )
        defineDestination( user )
    }

    fun goHomeWithEmail( email: String ){
        val intent = Intent(this@MainActivity,HomeActivity::class.java)
        intent.putExtra("user_email",email)
        startActivity(intent)
        finish()
    }

}