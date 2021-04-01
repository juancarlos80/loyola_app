package app.wiserkronox.loyolasocios.service.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assembly")
class Assembly () {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    //Primer metodo de autentificacion
    @ColumnInfo
    var name: String = ""
    @ColumnInfo
    var date: String = ""
    @ColumnInfo
    var journey: String = ""
    @ColumnInfo
    var memory: String = ""
    @ColumnInfo
    var statements: String = ""

    @ColumnInfo
    var zoom_url: String = ""
    @ColumnInfo
    var zoom_code: String = ""
    @ColumnInfo
    var zoom_password: String = ""

    @ColumnInfo
    var status: String = ""
    @ColumnInfo
    var created_at: String = ""
    @ColumnInfo
    var updated_at: String = ""
}