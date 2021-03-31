package app.wiserkronox.loyolasocios.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import app.wiserkronox.loyolasocios.R
import app.wiserkronox.loyolasocios.service.model.User


class UploadDataFragment : Fragment() {

    private val CURRENT_USER_KEY = "current_user_key"
    private var cUser: User? = null

    private lateinit var progress_data: ProgressBar
    private lateinit var progress_picture_1: ProgressBar
    private lateinit var progress_picture_2: ProgressBar
    private lateinit var progress_selfie: ProgressBar

    private lateinit var icon_data: ImageView
    private lateinit var icon_picture_1: ImageView
    private lateinit var icon_picture_2: ImageView
    private lateinit var icon_selfie: ImageView

    private lateinit var btnUpload: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cUser = it.getSerializable(CURRENT_USER_KEY) as User
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_upload_data, container, false)

        progress_data = root.findViewById(R.id.progress_data)
        progress_picture_1 = root.findViewById(R.id.progress_picture_1)
        progress_picture_2 = root.findViewById(R.id.progress_picture_2)
        progress_selfie = root.findViewById(R.id.progress_selfie)

        icon_data = root.findViewById(R.id.icon_data)
        icon_picture_1 = root.findViewById(R.id.icon_picture_1)
        icon_picture_2 = root.findViewById(R.id.icon_picture_2)
        icon_selfie = root.findViewById(R.id.icon_selfie)

        btnUpload = root.findViewById(R.id.btn_upload_server)
        btnCancel = root.findViewById(R.id.btn_cancel_upload)

        btnCancel.setOnClickListener{
            (activity as MainActivity).cancelRequests()
        }

        //Validamos que esten todos los datos
        cUser?.let {
            if (it.picture_1 == "" || it.picture_1 == "" || it.selfie == "" ) {
                it.state = User.REGISTER_DATA_STATE
                (activity as MainActivity).updateUser(it)
            }

            btnUpload.setOnClickListener {
                if( (activity as MainActivity).isOnline() ) {
                    sendUserData()
                } else {
                    (activity as MainActivity).showMessage("Debe estar conectado a Internet para poder enviar sus datos")
                    btnUpload.visibility = Button.VISIBLE
                }
            }
        }

        btnUpload.visibility = Button.INVISIBLE

        if( (activity as MainActivity).isOnline() ) {
            sendUserData()
        } else {
            (activity as MainActivity).showMessage("Debe estar conectado a Internet para poder enviar sus datos")
            btnUpload.visibility = Button.VISIBLE
        }
        return root
    }

    fun sendUserData(){
        cUser?.let {
            if( !it.data_online ){
                progress_data.isIndeterminate = true
                (activity as MainActivity).uploadUserDataServer( it )
            } else {
                sendUserImages(it)
            }
        }
    }

    fun sendUserImages(user: User){
        if( activity != null ) {
            var typeAuth = "email"
            var valueAuth = user.email
            if (user.oauth_uid != "") {
                typeAuth = "oauth_uid"
                valueAuth = user.oauth_uid
            }

            if (!user.picture_1_online) {
                progress_picture_1.isIndeterminate = true
                (activity as MainActivity).uploadUriServer(UPLOAD_TYPE_PICTURE_1, user.picture_1, typeAuth, valueAuth, user)
            } else if (!user.picture_2_online) {
                progress_picture_2.isIndeterminate = true
                (activity as MainActivity).uploadUriServer(UPLOAD_TYPE_PICTURE_2, user.picture_2, typeAuth, valueAuth, user)
            } else if (!user.selfie_online) {
                progress_selfie.isIndeterminate = true
                (activity as MainActivity).uploadUriServer(UPLOAD_TYPE_SELFIE, user.selfie, typeAuth, valueAuth, user)
            } else {
                user.state = User.COMPLETE_STATE
                user.state_activation = User.STATE_USER_INACTIVE
                (activity as MainActivity).backUpdate(user)
                (activity as MainActivity).goThanks()
            }
        }
    }

    fun terminateProgress( type: String, success: Boolean){
        when( type ){
            UPLOAD_TYPE_DATA -> {
                progress_data.isIndeterminate = false
                progress_data.progress = 100
                if (success) {
                    icon_data.setImageResource(R.drawable.ic_upload_ok)
                } else {
                    icon_data.setImageResource(R.drawable.ic_upload_fail)
                    btnUpload.visibility = Button.VISIBLE
                }
                icon_data.visibility = ImageView.VISIBLE
            }
            UPLOAD_TYPE_PICTURE_1 ->{
                progress_picture_1.isIndeterminate = false
                progress_picture_1.progress = 100
                if( success ) {
                    icon_picture_1.setImageResource(R.drawable.ic_upload_ok)
                }  else {
                    icon_picture_1.setImageResource(R.drawable.ic_upload_fail)
                    btnUpload.visibility = Button.VISIBLE
                }
                icon_picture_1.visibility = ImageView.VISIBLE
            }
            UPLOAD_TYPE_PICTURE_2 ->{
                progress_picture_2.isIndeterminate = false
                progress_picture_2.progress = 100
                if( success ) {
                    icon_picture_2.setImageResource(R.drawable.ic_upload_ok)
                } else {
                    icon_picture_2.setImageResource(R.drawable.ic_upload_fail)
                    btnUpload.visibility = Button.VISIBLE
                }
                icon_picture_2.visibility = ImageView.VISIBLE
            }
            UPLOAD_TYPE_SELFIE ->{
                progress_selfie.isIndeterminate = false
                progress_selfie.progress = 100
                if( success ) {
                    icon_selfie.setImageResource(R.drawable.ic_upload_ok)
                } else {
                    icon_selfie.setImageResource(R.drawable.ic_upload_fail)
                    btnUpload.visibility = Button.VISIBLE
                }
                icon_selfie.visibility = ImageView.VISIBLE
            }
        }
    }

    public companion object {
        //Tipos de imagen a subirse
        val UPLOAD_TYPE_DATA: String = "data"
        val UPLOAD_TYPE_PICTURE_1: String = "picture_1"
        val UPLOAD_TYPE_PICTURE_2: String = "picture_2"
        val UPLOAD_TYPE_SELFIE: String = "selfie"

        @JvmStatic
        fun newInstance(cUser: User) =
                UploadDataFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(CURRENT_USER_KEY, cUser)
                    }
                }
    }
}