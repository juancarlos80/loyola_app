package app.wiserkronox.loyolasocios.view.ui

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import app.wiserkronox.loyolasocios.R
import app.wiserkronox.loyolasocios.service.model.User



class PicturesFragment : Fragment() {

    private val CURRENT_USER_KEY = "current_user_key"
    private var cUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cUser = it.getSerializable(CURRENT_USER_KEY) as User
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(cUser: User) =
                PicturesFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(CURRENT_USER_KEY, cUser)
                    }
                }
    }

    private lateinit var id_member: EditText
    private lateinit var picture_1: ImageView
    private lateinit var picture_2: ImageView
    private lateinit var selfie: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root: View = inflater.inflate(R.layout.fragment_pictures, container, false)


        id_member = root.findViewById(R.id.edit_id_member)
        picture_1 = root.findViewById(R.id.img_picture_1)
        picture_2 = root.findViewById(R.id.img_picture_2)
        selfie = root.findViewById(R.id.img_selfie)

        val btnTakePicture1 = root.findViewById<Button>(R.id.btn_upload_picture_1)
        btnTakePicture1.setOnClickListener {
            saveidMember()
            ( activity as MainActivity).takePicture(CameraActivity.REQUEST_PICTURE_1)
        }

        val btnTakePicture2 = root.findViewById<Button>(R.id.btn_upload_picture_2)
        btnTakePicture2.setOnClickListener {
            ( activity as MainActivity).takePicture(CameraActivity.REQUEST_PICTURE_2)
        }

        val btnTakeSelfie = root.findViewById<Button>(R.id.btn_upload_selfie)
        btnTakeSelfie.setOnClickListener {
            ( activity as MainActivity).takePicture(CameraActivity.REQUEST_SELFIE)
        }

        val btnCancel = root.findViewById<Button>(R.id.btn_cancel_picture)
        btnCancel.setOnClickListener{
            ( activity as MainActivity).goWithoutSession()
        }

        val btnSave = root.findViewById<Button>(R.id.btn_save_picture)
        btnSave.setOnClickListener {
            validateRegister()
        }

        //Poblando el fragmento con la informacion del usuario
        cUser?.let {

            id_member.setText( it.id_member )

            if( it.picture_1 != ""){
                Log.d("TAG", "pic1: "+it.picture_1)
                Uri.parse( it.picture_1)?.let {
                     picture_1.setImageURI( it )
                }
            }
            if( it.picture_2 != ""){
                Uri.parse( it.picture_2)?.let {
                    picture_2.setImageURI( it )
                }
            }
            if( it.selfie != ""){
                Uri.parse( it.selfie)?.let {
                    selfie.setImageURI( it )
                }
            }

        }
        return root
    }

    fun saveidMember(){
        if( !TextUtils.isEmpty(id_member.text) ){
            cUser?.let {
                it.id_member = id_member.text.toString()
                (activity as MainActivity).backUpdate(it)
            }
        }
    }

    fun validateRegister(){
        if( TextUtils.isEmpty(id_member.text) ){
            Toast.makeText(activity, "Debes ingresar tu código de socio", Toast.LENGTH_SHORT).show()
            return
        }

        if( TextUtils.isEmpty(cUser?.picture_1) ){
            Toast.makeText(activity, "Debes subir la foto del anverso de tu ci", Toast.LENGTH_SHORT).show()
            return
        }
        if( TextUtils.isEmpty(cUser?.picture_2) ){
            Toast.makeText(activity, "Debes subir la foto del reverso de tu ci", Toast.LENGTH_SHORT).show()
            return
        }
        if( TextUtils.isEmpty(cUser?.picture_1) ){
            Toast.makeText(activity, "Debes subir tu una autofoto personal (selfie)", Toast.LENGTH_SHORT).show()
            return
        }

        cUser?.let{
            it.state = User.REGISTER_PICTURE_STATE
            ( activity as MainActivity).updateUser(it)
        }
    }

}