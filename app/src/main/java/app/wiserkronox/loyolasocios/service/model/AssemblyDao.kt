package app.wiserkronox.loyolasocios.service.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AssemblyDao {
    @Query("select * from assembly order by date")
    fun getAssemblys(): Flow<List<Assembly>>

    @Query("select * from assembly order by date")
    fun getAllAssemblys(): List<Assembly>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(assembly: Assembly): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(assembly: List<Assembly>): List<Long>

    @Query("delete from assembly")
    suspend fun deleteAll()

    @Query("select * from assembly where status = :status order by date")
    fun getAllAssemblysStatus(status: String): List<Assembly>

}