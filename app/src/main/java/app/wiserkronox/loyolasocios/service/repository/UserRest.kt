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



}