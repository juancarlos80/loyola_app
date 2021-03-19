package app.wiserkronox.loyolasocios.service.repository

import androidx.annotation.WorkerThread
import app.wiserkronox.loyolasocios.service.model.User
import app.wiserkronox.loyolasocios.service.model.UserDao
import kotlinx.coroutines.flow.Flow

class LoyolaRepository( private val userDao: UserDao ) {

    val allUsers: Flow<List<User>> = userDao.getUsers()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(user: User){
        userDao.insert( user )
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll(){
        userDao.deleteAll()
    }

    fun getUserEmail(email: String): Flow<User> {
        return userDao.getUserByEmail( email )
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(user: User){
        userDao.update(user)
    }
}