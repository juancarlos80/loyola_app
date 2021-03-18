package app.wiserkronox.loyolasocios.view.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import app.wiserkronox.loyolasocios.R
import com.google.android.gms.common.SignInButton

class WithoutSessionFragment : Fragment() {

    private lateinit var email: EditText
    private lateinit var password: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root: View = inflater.inflate(R.layout.fragment_without_session, container, false)

        email = root.findViewById(R.id.edit_email_login)
        password = root.findViewById(R.id.edit_password_login)

        //Funciones de los botones del fragmento
        val btnRegister = root.findViewById<Button>(R.id.btn_register)
        btnRegister.setOnClickListener {
            (activity as MainActivity).goManualRegister()
        }

        val btnGoogleSingin = root.findViewById<SignInButton>(R.id.btn_signin_google)
        setGoogleButtonText( btnGoogleSingin, getString(R.string.btn_singin_google) )

        btnGoogleSingin.setOnClickListener{
            (activity as MainActivity).signInGoogle()
        }

        val btnLogin = root.findViewById<Button>(R.id.btn_login)
        btnLogin.setOnClickListener{
            validarLogin()
        }

        return root
    }

    fun validarLogin(){
        if( TextUtils.isEmpty( email.text) ){
            Toast.makeText( activity, "Debes ingresar tu correo para ingresar", Toast.LENGTH_SHORT).show()
            return
        }
        if( TextUtils.isEmpty( password.text) ){
            Toast.makeText( activity, "Debes ingresar tu clave para ingresar", Toast.LENGTH_SHORT).show()
            return
        }
        ( activity as MainActivity).getUserByEmailPassword( email.text.toString(), password.text.toString() )
    }

    protected fun setGoogleButtonText(signInButton: SignInButton, buttonText: String?) {
        for (i in 0 until signInButton.childCount) {
            val v = signInButton.getChildAt(i)
            if (v is TextView) {
                v.text = buttonText
                return
            }
        }
    }

}