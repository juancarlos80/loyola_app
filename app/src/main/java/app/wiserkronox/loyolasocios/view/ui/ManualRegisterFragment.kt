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
import app.wiserkronox.loyolasocios.service.model.User

class ManualRegisterFragment : Fragment() {

    private lateinit var email_1: EditText
    private lateinit var password_1: EditText
    private lateinit var password_2: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root: View = inflater.inflate(R.layout.fragment_manual_register, container, false)

        email_1 = root.findViewById(R.id.edit_email_1)

        password_1 = root.findViewById(R.id.edit_password_1)
        password_2 = root.findViewById(R.id.edit_password_2)


        val btnCancel = root.findViewById<Button>(R.id.btn_cancel)
        btnCancel?.setOnClickListener{
            (activity as MainActivity?)!!.goWithoutSession()
        }

        val btnNext = root.findViewById<Button>(R.id.btn_next)
        btnNext?.setOnClickListener {
            validateRegister()
        }

        return root
    }

    fun validateRegister(){
        if( TextUtils.isEmpty( email_1.text) ){
            Toast.makeText( activity, "El email no puede estar vacio", Toast.LENGTH_SHORT).show()
            return
        }

        if( TextUtils.isEmpty( password_1.text) ){
            Toast.makeText( activity, "Debe definir una contraseñara el registro de su cuenta", Toast.LENGTH_SHORT).show()
            return
        }

        if( TextUtils.isEmpty( password_2.text) ){
            Toast.makeText( activity, "La confirmacion del password no puede estar vacia", Toast.LENGTH_SHORT).show()
            return
        }

        val user = User()
        user.email = email_1.text.toString()
        user.password = password_1.text.toString()

        (activity as MainActivity).registerManualUser( user )
    }

}