package app.wiserkronox.loyolasocios.service.repository

import android.content.Context
import app.wiserkronox.loyolasocios.R

class AssemblyRest (val context: Context){
    companion object{
        var GET_ASSEMBLYS = "get_assemblys.php"
    }

    fun getAssemblesURL(): String {
        return context.getString(R.string.host_service)+
                context.getString(R.string.home_service)+
                GET_ASSEMBLYS
    }

    /*

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
    }*/



}