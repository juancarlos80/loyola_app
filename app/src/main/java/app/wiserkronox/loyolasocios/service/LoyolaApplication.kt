package app.wiserkronox.loyolasocios.service

import android.app.Application
import app.wiserkronox.loyolasocios.service.model.AppRoomDataBase
import app.wiserkronox.loyolasocios.service.repository.LoyolaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class LoyolaApplication : Application () {

    val applicationScope = CoroutineScope( SupervisorJob() )

    val dataBase by lazy { AppRoomDataBase.getDataBase(this, applicationScope) }
    val repository by lazy { LoyolaRepository (dataBase.userDao()) }
}