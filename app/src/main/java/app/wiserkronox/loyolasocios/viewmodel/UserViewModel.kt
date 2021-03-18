package app.wiserkronox.loyolasocios.viewmodel

import androidx.lifecycle.*
import app.wiserkronox.loyolasocios.service.model.User
import app.wiserkronox.loyolasocios.service.repository.LoyolaRepository
import kotlinx.coroutines.launch

class UserViewModel (private val repository: LoyolaRepository) : ViewModel() {

    val allUsers: LiveData<List<User>> = repository.allUsers.asLiveData()

    fun insert(user: User) = viewModelScope.launch {
        repository.insert(user)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun getUserByEmail( email: String ) : LiveData<User>{
        return repository.getUserEmail(email).asLiveData()
    }

    /*fun getUserByEmail( email: String ) = viewModelScope.launch : LiveData<User> {
        repository.getUserEmail(email).asLiveData()
    }*/

}

class UserViewModelFactory( private val repository: LoyolaRepository ) : ViewModelProvider.Factory{
    override fun <T: ViewModel> create ( modelClass: Class<T>): T {
        if( modelClass.isAssignableFrom( UserViewModel::class.java) ){
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknow ViewModel Class")
    }
}

