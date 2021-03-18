package app.wiserkronox.loyolasocios.service.model

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@Database(
    entities = arrayOf(
        User::class
    ),
    version = 5,
    exportSchema = false
)

 abstract class AppRoomDataBase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppRoomDataBase? = null

        /*val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "DELETE FROM user"
                )
            }
        }*/

        fun getDataBase(
            context: Context,
            scope: CoroutineScope
        ) : AppRoomDataBase {
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppRoomDataBase::class.java,
                    "loyola_database"
                )
                    .addCallback(UserDataBaseCallBack(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class UserDataBaseCallBack(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.userDao())
                }
            }
        }

        suspend fun populateDatabase(userDao: UserDao){
            Log.d("ROOM", "populatin database")
            userDao.deleteAll()

            /*var user = User("Juans", "asdasd@asd.com", "asdsdd")
            userDao.insert(user)
            user = User("Victors", "sssaasdasd@hotma.com", "a42sdsdd")
            userDao.insert(user)*/
        }
    }

}
