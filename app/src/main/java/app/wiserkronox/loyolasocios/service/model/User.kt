package app.wiserkronox.loyolasocios.service.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
class User () {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

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

}



