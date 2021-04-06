package app.wiserkronox.loyolasocios.service.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "user")
class User () : Serializable {
    companion object {
        var REGISTER_LOGIN_STATE: String = "1"
        var REGISTER_DATA_STATE: String = "2"
        var REGISTER_PICTURE_STATE: String = "3"
        var UPLOAD_DATA_SERVER: String = "4"
        var COMPLETE_STATE: String = "5"
        var DOWNLOAD_DATA_SERVER: String = "6"

        var STATE_USER_ACTIVE: Int = 0
        var STATE_USER_INACTIVE: Int = 1
        var STATE_USER_REJECT: Int = 2
    }

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    //Primer metodo de autentificacion
    @ColumnInfo
    var oauth_uid: String = ""
    @ColumnInfo
    var oauth_provider: String = ""

    //Segunoo metodo de autentificacion
    @ColumnInfo
    var email: String = ""
    @ColumnInfo
    var password: String = ""


    @ColumnInfo
    var names: String = ""
    @ColumnInfo
    var last_name_1: String = ""
    @ColumnInfo
    var last_name_2: String = ""
    @ColumnInfo
    var gender: String = ""
    @ColumnInfo
    var picture: String = ""
    @ColumnInfo
    var id_number: Int = 0
    @ColumnInfo
    var extension: String = ""
    @ColumnInfo
    var birthdate: String = ""
    @ColumnInfo
    var id_member: String = ""
    @ColumnInfo
    var picture_1: String = ""
    @ColumnInfo
    var picture_2: String = ""
    @ColumnInfo
    var selfie: String = ""
    @ColumnInfo
    var verification_code: String = ""
    @ColumnInfo
    var state: String = ""
    @ColumnInfo
    var phone_number: String = ""

    @ColumnInfo
    var data_online: Boolean = false
    @ColumnInfo
    var picture_1_online: Boolean = false
    @ColumnInfo
    var picture_2_online: Boolean = false
    @ColumnInfo
    var selfie_online: Boolean = false

    @ColumnInfo
    var state_activation: Int = STATE_USER_INACTIVE
    @ColumnInfo
    var feedback_activation: String = ""
    @ColumnInfo
    var feedback_date: String = ""
}



