package app.wiserkronox.loyolasocios.view.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import app.wiserkronox.loyolasocios.service.LoyolaApplication
import app.wiserkronox.loyolasocios.service.model.User

class HomeViewModel : ViewModel() {

    /*private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }*/

    private val _user = MutableLiveData<User>().apply {
        value = LoyolaApplication.getInstance()?.user
    }

    //val text: LiveData<String> = _text
    val user: LiveData<User> = _user
}