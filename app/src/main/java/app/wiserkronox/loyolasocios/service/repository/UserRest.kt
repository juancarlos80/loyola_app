package app.wiserkronox.loyolasocios.service.repository

import android.content.Context
import app.wiserkronox.loyolasocios.R
import app.wiserkronox.loyolasocios.service.model.User
import org.json.JSONException
import org.json.JSONObject

class UserRest (val context: Context){
    companion object{
        var SET_USER_DATA = "set_user_data.php"
        var SET_USER_PICTURE = "upload_user_picture.php"
        var GET_USER_STATUS = "get_user_status.php"
        var GET_USER_LOGIN = "get_user_login.php"
    }

    fun getUserDataURL(): String {
        return context.getString(R.string.host_service)+
                context.getString(R.string.home_service)+
                SET_USER_DATA
    }

    fun getUserPictureURL(): String {
        return context.getString(R.string.host_service)+
                context.getString(R.string.home_service)+
                SET_USER_PICTURE
    }

    fun getUserStatusURL(): String {
        return context.getString(R.string.host_service)+
                context.getString(R.string.home_service)+
                GET_USER_STATUS
    }

    fun getUserLoginURL(): String {
        return context.getString(R.string.host_service)+
                context.getString(R.string.home_service)+
                GET_USER_LOGIN
    }

    fun getUserDataJson(user: User, upDate: Boolean): JSONObject? {
        val jsonBody = JSONObject()
        try {
            jsonBody.put("names", user.names)
            jsonBody.put("last_name_1", user.last_name_1)
            if( !user.last_name_2.equals("") ){
                jsonBody.put("last_name_2", user.last_name_2)
            }
            jsonBody.put("id_number", user.id_number)
            jsonBody.put("id_member", user.id_member)
            jsonBody.put("extension", user.extension)
            jsonBody.put("birthdate", user.birthdate)
            jsonBody.put("phone_number", user.phone_number)

            if( !user.oauth_uid.equals("") ){
                jsonBody.put("oauth_uid", user.oauth_uid)
                jsonBody.put("oauth_provider", user.oauth_provider)
            }

            if( !user.email.equals("") ) {
                jsonBody.put("email", user.email)
            }

            if( !user.password.equals("") ) {
                jsonBody.put("password", user.password)
            }
            if( upDate ){
                jsonBody.put("update_user", upDate)
            }

            return jsonBody
        } catch (e: JSONException) {
            e.printStackTrace()
            return null
        }
    }

    fun getUserDataLoginJson(email: String, password: String, oauth_uid: String): JSONObject? {
        val jsonBody = JSONObject()
        return try {
            if( oauth_uid != "" ){
                jsonBody.put("oauth_uid", oauth_uid )
            } else {
                jsonBody.put("email", email)
                jsonBody.put("password", password)
            }
            jsonBody
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

    fun getUserLoginJson(user: User): JSONObject? {
        val jsonBody = JSONObject()
        return try {
            if( !user.oauth_uid.equals("") ){
                jsonBody.put("oauth_uid", user.oauth_uid)
                jsonBody.put("oauth_provider", user.oauth_provider)
            }

            if( !user.email.equals("") ) {
                jsonBody.put("email", user.email)
            }

            jsonBody
        } catch (e: JSONException) {
            e.printStackTrace()
            null
        }
    }

    fun getUserFromJSON( j_user: JSONObject ): User?{
        return try{
            val user = User()
            user.names = j_user.getString("names")
            user.last_name_1 = j_user.getString("last_name_1")
            if( !j_user.isNull("last_name_2") ) {
                user.last_name_2 = j_user.getString("last_name_2")
            }
            if( !j_user.isNull("oauth_provider") ) {
                user.oauth_provider = j_user.getString("oauth_provider")
            }
            if( !j_user.isNull("oauth_uid") ) {
                user.oauth_uid = j_user.getString("oauth_uid")
            }
            if( !j_user.isNull("email") ) {
                user.email = j_user.getString("email")
            }
            if( !j_user.isNull("password") ) {
                user.password = j_user.getString("password")
            }
            if( !j_user.isNull("id_number") ) {
                user.id_number = j_user.getInt("id_number")
            }
            if( !j_user.isNull("id_extension") ) {
                user.extension = j_user.getString("id_extension")
            }
            if( !j_user.isNull("birthday") ) {
                user.birthdate = j_user.getString("birthday")
            }
            if( !j_user.isNull("phone_number") ) {
                user.phone_number = j_user.getString("phone_number")
            }
            if( !j_user.isNull("id_member") ) {
                user.id_member = j_user.getString("id_member")
            }
            if( !j_user.isNull("picture_id_1") ) {
                user.picture_1 = context.getString(R.string.host_service)+
                        context.getString(R.string.home_aplication)+
                        context.getString(R.string.dir_photo_service)+
                        j_user.getLong("id")+"/"+
                        j_user.getString("picture_id_1")
            }
            if( !j_user.isNull("picture_id_2") ) {
                user.picture_2 = context.getString(R.string.host_service)+
                        context.getString(R.string.home_aplication)+
                        context.getString(R.string.dir_photo_service)+
                        j_user.getLong("id")+"/"+
                        j_user.getString("picture_id_2")
            }
            if( !j_user.isNull("selfie") ) {
                user.selfie = context.getString(R.string.host_service)+
                        context.getString(R.string.home_aplication)+
                        context.getString(R.string.dir_photo_service)+
                        j_user.getLong("id")+"/"+
                        j_user.getString("selfie")
            }
            if( !j_user.isNull("verification_code") ) {
                user.verification_code = j_user.getString("verification_code")
            }
            if( !j_user.isNull("state") ) {
                if(j_user.getString("state") == "activo"){
                    user.state_activation = User.STATE_USER_ACTIVE
                } else {
                    user.state_activation = User.STATE_USER_REJECT
                }
            } else {
                user.state_activation = User.STATE_USER_INACTIVE
            }

            user.state = User.DOWNLOAD_DATA_SERVER
            user
        } catch (e: JSONException){
            e.printStackTrace()
            null
        }
    }

    fun updateUserFromServer(user_l:User, user_s: User):User{
        if( user_l.names != user_s.names ){
            user_l.names = user_s.names
        }
        if( user_l.last_name_1 != user_s.last_name_1 ){
            user_l.last_name_1 = user_s.last_name_1
        }
        if( user_l.last_name_2 != user_s.last_name_2 ){
            user_l.last_name_2 = user_s.last_name_2
        }
        if( user_l.oauth_provider != user_s.oauth_provider ){
            user_l.oauth_provider = user_s.oauth_provider
        }
        if( user_l.oauth_uid != user_s.oauth_uid ){
            user_l.oauth_uid = user_s.oauth_uid
        }
        if( user_l.email != user_s.email ){
            user_l.email = user_s.email
        }
        if( user_l.password != user_s.password ){
            user_l.password = user_s.password
        }
        if( user_l.id_number != user_s.id_number ){
            user_l.id_number = user_s.id_number
        }
        if( user_l.extension != user_s.extension ){
            user_l.extension = user_s.extension
        }
        if( user_l.birthdate != user_s.birthdate ){
            user_l.birthdate = user_s.birthdate
        }
        if( user_l.phone_number != user_s.phone_number ){
            user_l.phone_number = user_s.phone_number
        }
        if( user_l.id_member != user_s.id_member ){
            user_l.id_member = user_s.id_member
        }
        if( user_l.picture_1 != user_s.picture_1 ){
            user_l.picture_1 = user_s.picture_1
            user_l.state = User.DOWNLOAD_DATA_SERVER
        }
        if( user_l.picture_2 != user_s.picture_2 ){
            user_l.picture_2 = user_s.picture_2
            user_l.state = User.DOWNLOAD_DATA_SERVER
        }
        if( user_l.selfie != user_s.selfie ){
            user_l.selfie = user_s.selfie
            user_l.state = User.DOWNLOAD_DATA_SERVER
        }
        if( user_l.state_activation != user_s.state_activation ){
            user_l.state_activation = user_s.state_activation
        }
        return user_l
    }

}