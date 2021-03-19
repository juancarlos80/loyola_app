package app.wiserkronox.loyolasocios.service.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow


@Dao
interface UserDao {
    @Query("select * from user order by names")
    fun getUsers(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User?)


    @Query("select * from user where email = :email")
    fun getUserByEmail(email: String): Flow<User>

    @Query("select * from user where email = :email and password = :password")
    fun getUserByEmailPassword(email: String, password: String): Flow<User>

    @Query("select * from user where oauth_uid = :oauth_uid")
    fun getUserByOauth_uid(oauth_uid: String): Flow<User>

    @Query("delete from user")
    suspend fun deleteAll()
}