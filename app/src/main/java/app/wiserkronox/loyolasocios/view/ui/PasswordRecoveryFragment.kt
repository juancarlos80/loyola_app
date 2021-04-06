package app.wiserkronox.loyolasocios.view.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import app.wiserkronox.loyolasocios.R


class PasswordRecoveryFragment : Fragment() {

    private lateinit var emailRecover: EditText
    private lateinit var btnRecover: Button
    private lateinit var btnBack: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_password_recovery, container, false)

        emailRecover = root.findViewById(R.id.edit_email_recovery)
        btnRecover = root.findViewById(R.id.btn_recovery)
        btnBack = root.findViewById(R.id.btn_back)

        btnBack.setOnClickListener {
            (activity as MainActivity).goWithoutSession()
        }

        btnRecover.setOnClickListener {
            validateData()
        }

        return root
    }

    fun validateData(){
        if( TextUtils.isEmpty(emailRecover.text) ){
            Toast.makeText(
                activity,
                "Debes ingresar tu correo electrónico para poder restablecer tu contraseña",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if ( !android.util.Patterns.EMAIL_ADDRESS.matcher(emailRecover.text).matches() ){
            Toast.makeText(
                activity,
                "El correo electrónico no es una direccion válida o contiene caracteres no permitidos",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        ( activity as MainActivity ).postPasswordRecovery(emailRecover.text.toString())
    }

}