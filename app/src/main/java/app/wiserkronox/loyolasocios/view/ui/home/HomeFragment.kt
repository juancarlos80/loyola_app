package app.wiserkronox.loyolasocios.view.ui.home

import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.wiserkronox.loyolasocios.R
import app.wiserkronox.loyolasocios.service.LoyolaApplication
import app.wiserkronox.loyolasocios.service.model.User


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    private  lateinit var userSelfie: ImageView
    private  lateinit var userName: TextView
    private  lateinit var userIDMember: TextView
    private  lateinit var userStatus: ImageView
    private  lateinit var inActiveStatus: TextView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_home, container, false)

        userName = root.findViewById(R.id.text_user_fullname)
        userSelfie = root.findViewById(R.id.image_selfie)
        userIDMember = root.findViewById(R.id.text_user_id_member)
        userStatus = root.findViewById(R.id.image_status_user)
        inActiveStatus = root.findViewById(R.id.text_inactive_status)

        val user = LoyolaApplication.getInstance()?.user

        Log.d("Frag", "useer session: " + user?.selfie)

        user.let {
            userName.text = it?.names+" "+it?.last_name_1+" "+it?.last_name_2
            userIDMember.text = it?.id_member

            if( it?.state == User.ACTIVE_STATE ){
                userStatus.setImageDrawable( activity?.getDrawable(R.drawable.icon_status_user_active))
                inActiveStatus.visibility = TextView.GONE
            } else {
                userStatus.setImageDrawable( activity?.getDrawable(R.drawable.icon_status_user_inactive))
                inActiveStatus.visibility = TextView.VISIBLE
            }

            val src = MediaStore.Images.Media.getBitmap(activity?.getContentResolver(), Uri.parse(it?.selfie))
            if( src != null ) {
                val dr = RoundedBitmapDrawableFactory.create(resources, src)
                dr.cornerRadius = Math.max(src.width, src.height) / 2.0f
                userSelfie.setImageDrawable(dr)
            }
        }




        /*homeViewModel.user.observe(viewLifecycleOwner, Observer {
            val res: Resources = resources
            val inputStream: InputStream? = activity?.baseContext?.contentResolver?.openInputStream(Uri.parse(it.selfie))
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            val src = BitmapFactory.decodeStream(inputStream, null, options);
            if( src != null ) {
                val dr = RoundedBitmapDrawableFactory.create(res, src)
                dr.cornerRadius = Math.max(src.width, src.height) / 2.0f
                userSelfie.setImageDrawable(dr)
            }
        })*/
        return root
    }
}