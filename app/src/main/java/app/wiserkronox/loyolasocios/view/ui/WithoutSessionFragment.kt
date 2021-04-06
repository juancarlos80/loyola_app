package app.wiserkronox.loyolasocios.view.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import app.wiserkronox.loyolasocios.R
import com.google.android.gms.common.SignInButton
import com.google.android.material.snackbar.Snackbar

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
            validateLogin(it)
        }

        val btnRecovery = root.findViewById<TextView>(R.id.text_password_recovery)
        btnRecovery.setOnClickListener{
            (activity as MainActivity).goPasswordRecovery()
        }
        return root
    }

    private fun validateLogin(view : View){
        if( TextUtils.isEmpty( email.text) ){
            Snackbar.make (view, "Debes ingresar tu direccion de correo para ingresar al sistema", Snackbar.LENGTH_LONG).show()
            return
        }
        if( TextUtils.isEmpty( password.text) ){
            Snackbar.make (view, "Debes ingresar la clave de tu para ingresar al sistema", Snackbar.LENGTH_LONG).show()
            return
        }

        ( activity as MainActivity).getUserByEmailPassword( email.text.toString(), password.text.toString() )
    }

    private fun setGoogleButtonText(signInButton: SignInButton, buttonText: String?) {
        for (i in 0 until signInButton.childCount) {
            val v = signInButton.getChildAt(i)
            if (v is TextView) {
                v.text = buttonText
                return
            }
        }
    }

}