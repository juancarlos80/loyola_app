package app.wiserkronox.loyolasocios.view.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import app.wiserkronox.loyolasocios.R
import app.wiserkronox.loyolasocios.service.model.User
import com.google.android.material.switchmaterial.SwitchMaterial


class TermsFragment : Fragment() {

    private val CURRENT_USER_KEY = "current_user_key"
    private var cUser: User? = null

    private lateinit var switch: SwitchMaterial
    private lateinit var btnAcept: Button
    private lateinit var btnCancel: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            cUser = it.getSerializable(CURRENT_USER_KEY) as User
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(cUser: User) =
            TermsFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(CURRENT_USER_KEY, cUser)
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_terms, container, false)

        switch = root.findViewById(R.id.switch_acepto)
        btnAcept = root.findViewById(R.id.btn_term_acept)
        btnCancel = root.findViewById(R.id.btn_term_cancel)

        switch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                btnAcept.visibility = Button.VISIBLE
                btnCancel.visibility = Button.GONE
            } else {
                btnAcept.visibility = Button.GONE
                btnCancel.visibility = Button.VISIBLE
            }
        }
        cUser?.let { user ->
            btnAcept.setOnClickListener {
                user.state = User.UPLOAD_DATA_SERVER
                (activity as MainActivity).updateUser( user )
            }
        }

        return root
    }

}