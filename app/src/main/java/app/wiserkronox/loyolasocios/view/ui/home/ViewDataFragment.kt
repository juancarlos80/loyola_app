package app.wiserkronox.loyolasocios.view.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import app.wiserkronox.loyolasocios.R
import app.wiserkronox.loyolasocios.service.LoyolaApplication
import app.wiserkronox.loyolasocios.view.ui.HomeActivity

class ViewDataFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_view_data, container, false)

        val user = LoyolaApplication.getInstance()?.user
        if( user != null ){
            //Populate obbject
            root.findViewById<TextView>(R.id.text_names).text = user.names
            root.findViewById<TextView>(R.id.text_last_names).text = user.last_name_1+" "+user.last_name_2
            root.findViewById<TextView>(R.id.text_id_number).text = user.id_number.toString()+" "+user.extension
            root.findViewById<TextView>(R.id.text_birthday).text = user.birthdate
            root.findViewById<TextView>(R.id.text_phone_number).text = user.phone_number
            root.findViewById<TextView>(R.id.text_email).text = user.email
        } else {
            Toast.makeText( context, "No se encontro el usuario", Toast.LENGTH_SHORT).show()
            ( activity as HomeActivity).goHome()
        }
        return root
    }


}