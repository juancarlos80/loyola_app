package app.wiserkronox.loyolasocios.service.repository

import android.content.Context
import app.wiserkronox.loyolasocios.R
import app.wiserkronox.loyolasocios.service.model.Assembly
import org.json.JSONArray
import org.json.JSONException

class AssemblyRest (val context: Context){
    companion object{
        var GET_ASSEMBLYS = "get_assemblys.php"
    }

    fun getAssemblesURL(): String {
        return context.getString(R.string.host_service)+
                context.getString(R.string.home_service)+
                GET_ASSEMBLYS
    }

    fun getAssemblysList(jAssemblys: JSONArray): List<Assembly>{
        var listAssembly : ArrayList<Assembly> = arrayListOf()
        try{
            for ( i in 0..jAssemblys.length()-1 ){
                val j_assembly = jAssemblys.getJSONObject(i)
                var assembly = Assembly()
                assembly.id = j_assembly.getLong("id")
                assembly.name = j_assembly.getString("name")
                assembly.date = j_assembly.getString("datetime")
                assembly.journey = j_assembly.getString("journey")
                if( j_assembly.getString("memory").length > 10 ) {
                    assembly.memory = context.getString(R.string.host_service) + j_assembly.getString("memory").substring(2)
                }
                if( j_assembly.getString("statemts").length > 10 ) {
                    assembly.statements = context.getString(R.string.host_service) + j_assembly.getString("statemts").substring(2)
                }
                assembly.status = j_assembly.getString("status")
                assembly.zoom_code = j_assembly.getString("zoom_code")
                assembly.zoom_password = j_assembly.getString("zoom_password")
                assembly.created_at = j_assembly.getString("created_at")
                assembly.updated_at = j_assembly.getString("updated_at")

                listAssembly.add(assembly)
            }
            return listAssembly

        } catch (j_error: JSONException){
            j_error.printStackTrace()
            return listAssembly
        }
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