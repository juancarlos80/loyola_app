package app.wiserkronox.loyolasocios.view.ui.home

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import app.wiserkronox.loyolasocios.R
import app.wiserkronox.loyolasocios.service.LoyolaApplication

class ViewPicturesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_view_pictures, container, false)
        val user = LoyolaApplication.getInstance()?.user
        if( user != null ) {
            root.findViewById<TextView>(R.id.text_id_member).text = user.id_member
            root.findViewById<ImageView>(R.id.image_picture_1).setImageURI( Uri.parse(user.picture_1) )
            root.findViewById<ImageView>(R.id.image_picture_2).setImageURI( Uri.parse(user.picture_2) )
            root.findViewById<ImageView>(R.id.image_selfiev).setImageURI( Uri.parse(user.selfie) )
        }
        return root
    }

}