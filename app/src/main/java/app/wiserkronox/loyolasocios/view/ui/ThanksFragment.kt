package app.wiserkronox.loyolasocios.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import app.wiserkronox.loyolasocios.R
import app.wiserkronox.loyolasocios.service.model.User


class ThanksFragment : Fragment() {

    private val CURRENT_USER_KEY = "current_user_key"
    private var cUser: User? = null

    private lateinit var btnAcept: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cUser = it.getSerializable(CURRENT_USER_KEY) as User
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_thanks, container, false)

        btnAcept = root.findViewById(R.id.btn_upload_server)
        btnAcept.setOnClickListener{
            (activity as MainActivity).goHome()
        }

        return root
    }

    companion object {
        @JvmStatic
        fun newInstance(cUser: User) =
                ThanksFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable(CURRENT_USER_KEY, cUser)
                    }
                }
    }
}