package app.wiserkronox.loyolasocios.service.repository

import androidx.annotation.WorkerThread
import app.wiserkronox.loyolasocios.service.model.Assembly
import app.wiserkronox.loyolasocios.service.model.AssemblyDao
import app.wiserkronox.loyolasocios.service.model.User
import app.wiserkronox.loyolasocios.service.model.UserDao
import kotlinx.coroutines.flow.Flow

class LoyolaRepository(
    private val userDao: UserDao,
    private val assemblyDao: AssemblyDao) {

    val allUsers: Flow<List<User>> = userDao.getUsers()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(user: User): Long {
        return userDao.insert( user )
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update2(user: User): Int {
        return userDao.update2( user )
    }

    /*fun insert2(user: User): Flow<Long>{
        return userDao.insert2( user )
    }*/

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll(){
        userDao.deleteAll()
    }


    fun getUserEmail(email: String): User {
        return userDao.getUserByEmail( email )
    }

    fun getUserEmail2(email: String): Flow<User> {
        return userDao.getUserByEmail2( email )
    }

    fun getUserByOauthUid(oauthUid: String): User {
        return userDao.getUserByOauth_uid( oauthUid)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun update(user: User){
        userDao.update(user)
    }

    /*************************************************************************************
     * Funciones para la instancia de la asamblea
     */
    val allAssemblys: Flow<List<Assembly>> = assemblyDao.getAssemblys()
    fun getAllAssembly(): List<Assembly> {
        return assemblyDao.getAllAssemblys()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertAssembly(assembly: Assembly): Long {
        return assemblyDao.insert( assembly )
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertAllAssembly(assemblys: List<Assembly>): List<Long>{
        return assemblyDao.insertAll( assemblys )
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAllAssemblys(){
        assemblyDao.deleteAll()
    }

    fun getAllAssemblysStatus(status: String): List<Assembly> {
        return assemblyDao.getAllAssemblysStatus(status)
    }}