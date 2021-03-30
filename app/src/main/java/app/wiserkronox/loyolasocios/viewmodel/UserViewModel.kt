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

    /*fun insert2(user: User): LiveData<Long> {
        return repository.insert2(user).asLiveData()
    }*/

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }

    fun update(user: User) = viewModelScope.launch {
        repository.update(user)
    }

    /*fun getByOautdID(email: String): User {
        return repository.getUserEmail(email)
    }*/

    /*fun getUserByEmail2( email: String ) : Flow<User> {
        return repository.getUserEmail2(email)
    }*/

    /*fun getUserByEmail2( email: String ) = viewModelScope.launch {
        repository.getUserEmail2(email).asLiveData()
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

