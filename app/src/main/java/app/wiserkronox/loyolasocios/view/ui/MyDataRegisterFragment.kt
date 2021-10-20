package app.wiserkronox.loyolasocios.view.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import app.wiserkronox.loyolasocios.R
import app.wiserkronox.loyolasocios.service.model.User
import app.wiserkronox.loyolasocios.viewmodel.MyDataViewModel


class MyDataRegisterFragment : Fragment() {

    private lateinit var myDataViewModel: MyDataViewModel

    private lateinit var names: EditText
    private lateinit var last_name_1: EditText
    private lateinit var last_name_2: EditText
    private lateinit var id_number: EditText
    private lateinit var extension: Spinner
    //private lateinit var email_1: EditText
    private lateinit var birthday: TextView
    private lateinit var phone_number: EditText
    private lateinit var text_feedback: TextView

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
            MyDataRegisterFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(CURRENT_USER_KEY, cUser)
                }
            }
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        myDataViewModel =
                ViewModelProvider(this).get(MyDataViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_my_data_register, container, false)

        names = root.findViewById(R.id.edit_names)
        last_name_1 = root.findViewById(R.id.edit_last_name_1)
        last_name_2 = root.findViewById(R.id.edit_last_name_2)
        id_number = root.findViewById(R.id.edit_id_number)
        extension = root.findViewById(R.id.spinner_extension)
        birthday = root.findViewById(R.id.text_birthdate)
        phone_number = root.findViewById(R.id.edit_phone_number)
        text_feedback = root.findViewById(R.id.text_feedback)

        birthday.setOnClickListener {
            val lDate = birthday.text.split("/")
            var yy = 1980
            var mm=0
            var dd=1
            if( lDate.size == 3 ){
                yy = Integer.parseInt(lDate[0])
                mm = Integer.parseInt(lDate[1])-1
                dd = Integer.parseInt(lDate[2])
            }
            val dp = DatePickerDialog(it.context, R.style.MySpinnerDatePickerStyle, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                birthday.text = year.toString() + "/" + (monthOfYear + 1) + "/" + dayOfMonth
            }, yy, mm, dd)

            dp.show()
        }

        activity?.let {
            ArrayAdapter.createFromResource(
                    it.applicationContext,
                    R.array.list_extensions,
                    android.R.layout.simple_spinner_item
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                extension.adapter = adapter
            }
        }

        cUser?.let {
            names.setText(it.names)
            last_name_1.setText(it.last_name_1)
            last_name_2.setText(it.last_name_2)
            id_number.setText(if (it.id_number > 0) it.id_number.toString() else "")
            extension.setSelection(resources.getStringArray(R.array.list_extensions).indexOf(it.extension))
            if(it.birthdate!="" ) {
                birthday.text = it.birthdate
            }
            phone_number.setText(it.phone_number)

            if( it.feedback_activation != ""){
                text_feedback.text = it.feedback_activation
                text_feedback.visibility = TextView.VISIBLE
            }
        }

        val btnSave = root.findViewById<Button>(R.id.btn_save)
        btnSave.setOnClickListener {
            validateUserData()
        }

        val btnCancel = root.findViewById<Button>(R.id.btn_cancel_data)
        btnCancel.setOnClickListener {
            ( activity as MainActivity).goWithoutSession()
        }

        return root
    }

    fun validateUserData(){
        if( TextUtils.isEmpty(names.text) ){
            Toast.makeText(activity, "Debes ingresar tu nombre", Toast.LENGTH_SHORT).show()
            return
        }
        if( TextUtils.isEmpty(last_name_1.text) ){
            Toast.makeText(activity, "No escribiste tu primer apellido", Toast.LENGTH_SHORT).show()
            return
        }
        if( TextUtils.isEmpty(id_number.text) ){
            Toast.makeText(activity, "Debes ingresar tu numero de identificación", Toast.LENGTH_SHORT).show()
            return
        }

        if( birthday.text == getString(R.string.hint_birthday) ){
            Toast.makeText(activity, "No escribiste la fecha de tu nacimiento", Toast.LENGTH_SHORT).show()
            return
        }

        if( TextUtils.isEmpty(phone_number.text) ){
            Toast.makeText(activity, "Te falto el numero de tu celular", Toast.LENGTH_SHORT).show()
            return
        }

        if( phone_number.text.toString().length < 8 ||
                ( !phone_number.text.toString().startsWith("6") && !phone_number.text.toString().startsWith("7") ) ){
            Toast.makeText(activity, "El número de telefono debe ser de 8 digitos y empezar con 6 o 7", Toast.LENGTH_LONG).show()
            return
        }

        saveUserData()
    }

    fun saveUserData(){
        cUser?.let{
            it.names = names.text.toString()
            it.last_name_1 = last_name_1.text.toString()
            it.last_name_2 = last_name_2.text.toString()
            it.id_number = id_number.text.toString().toInt()
            it.extension = extension.selectedItem.toString()
            it.birthdate = birthday.text.toString()
            it.phone_number = phone_number.text.toString()

            it.state = User.REGISTER_DATA_STATE

            ( activity as MainActivity ).updateUser(it)
        }
    }
}