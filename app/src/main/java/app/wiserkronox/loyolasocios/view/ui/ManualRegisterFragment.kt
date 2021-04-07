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
import android.util.Log

class ManualRegisterFragment : Fragment() {

    private lateinit var emailEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var passwordEditC: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val root: View = inflater.inflate(R.layout.fragment_manual_register, container, false)

        emailEdit = root.findViewById(R.id.edit_email_1)
        passwordEdit = root.findViewById(R.id.edit_password_1)
        passwordEditC = root.findViewById(R.id.edit_password_2)


        val btnCancel = root.findViewById<Button>(R.id.btn_cancel)
        btnCancel?.setOnClickListener{
            (activity as MainActivity?)!!.goWithoutSession()
        }

        val btnNext = root.findViewById<Button>(R.id.btn_next)
        btnNext?.setOnClickListener {
            validateRegister()
        }

        Log.d("FX", "update")

        return root
    }

    fun validateRegister(){
        if( TextUtils.isEmpty(emailEdit.text) ){
            Toast.makeText(
                activity,
                "El correo electronico no puede estar vacio",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if ( !android.util.Patterns.EMAIL_ADDRESS.matcher(emailEdit.text).matches() ){
            Toast.makeText(
                activity,
                "El correo electrónico no es una direccion válida o contiene caracteres no permitidos",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if( TextUtils.isEmpty(passwordEdit.text) ){
            Toast.makeText(
                activity,
                "Debe definir una contraseñara el registro de su cuenta",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if( passwordEdit.text.toString().length < 6 ){
            Toast.makeText(
                activity,
                "La contraseña debe tener al menos 6 caracteres",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if( TextUtils.isEmpty(passwordEditC.text) ){
            Toast.makeText(
                activity,
                "La confirmacion de la contraseña no puede estar vacia",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if( !passwordEdit.text.toString().equals(passwordEditC.text.toString()) ){
            Toast.makeText(activity, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
            return
        }


        (activity as MainActivity).checkLocalUser( emailEdit.text.toString(), passwordEdit.text.toString() )
        //(activity as MainActivity).goLoader()
        /*GlobalScope.launch {
            val user_reg = LoyolaApplication.getInstance()?.repository?.getUserByEmail(email_1.text.toString())
            if( user_reg != null ){
                (activity as MainActivity).goFailLogin("El correo electrónico que intenta registrar ya esta en uso")
            } else {
                val user = User()
                user.email = email_1.text.toString()
                user.password = password_1.text.toString()
                user.state = User.REGISTER_LOGIN_STATE

                if( activity == null )
                    Log.d("FK", "Nula que hacer??")
                else {
                    Log.d("FK", "Nooo Ac nula")
                }

                (activity as MainActivity).registerManualUser(user)
            }
        }*/
    }
}