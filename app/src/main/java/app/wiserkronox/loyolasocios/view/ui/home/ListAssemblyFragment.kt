package app.wiserkronox.loyolasocios.view.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.wiserkronox.loyolasocios.R
import app.wiserkronox.loyolasocios.service.LoyolaApplication
import app.wiserkronox.loyolasocios.service.model.Assembly
import app.wiserkronox.loyolasocios.service.repository.AssemblyRest
import app.wiserkronox.loyolasocios.service.repository.LoyolaService
import app.wiserkronox.loyolasocios.view.adapter.AdapterAssembly
import app.wiserkronox.loyolasocios.view.ui.HomeActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException


class ListAssemblyFragment : Fragment() {

    private  lateinit var loader: ProgressBar
    private  lateinit var recycler: RecyclerView

    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_list_assembly, container, false)

        loader = root.findViewById(R.id.progress_assembly)
        recycler = root.findViewById(R.id.recyclerAssembly)


        getUpdateFromServer()
        return root
    }

    fun getUpdateFromServer(){
        activity?.let{
            val assemblyRest = AssemblyRest(it)
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST, assemblyRest.getAssemblesURL(),
                null,
                { response ->
                    Log.d(TAG, "Response is: ${response.toString()}")
                    if (response.getBoolean("success")) {
                        Log.d(TAG, "Exito")
                        if (response.has("assemblys")) {
                            val assemblys = response.getJSONArray("assemblys")
                            if (assemblys.length() > 0) {
                                updateAssemblys(assemblys)
                            }
                        }
                    } else {
                        (activity as HomeActivity).showMessage(response.getString("reason"))
                    }
                },
                { error ->
                    Log.e(TAG, error.toString())
                    error.printStackTrace()
                    (activity as HomeActivity).showMessage("Error de conexión con el servidor")
                }
            )

            // Add the request to the RequestQueue.
            LoyolaService.getInstance(it).addToRequestQueue(jsonObjectRequest)
        }
    }

    fun updateAssemblys(jAssemblys: JSONArray){
        try{
            var listAssembly : ArrayList<Assembly> = arrayListOf()
            for ( i in 0..jAssemblys.length()-1 ){
                val j_assembly = jAssemblys.getJSONObject(i)
                var assembly = Assembly()
                assembly.id = j_assembly.getLong("id")
                assembly.name = j_assembly.getString("name")
                assembly.date = j_assembly.getString("datetime")
                assembly.journey = j_assembly.getString("journey")
                assembly.memory = j_assembly.getString("memory")
                assembly.status = j_assembly.getString("status")
                assembly.created_at = j_assembly.getString("created_at")
                assembly.updated_at = j_assembly.getString("updated_at")

                listAssembly.add(assembly)
            }
            insertAssemblys(listAssembly)

        } catch (j_error: JSONException){
            j_error.printStackTrace()
        }
    }

    fun insertAssemblys(assemblys: List<Assembly>){
        GlobalScope.launch {
            LoyolaApplication.getInstance()?.repository?.deleteAllAssemblys()
            LoyolaApplication.getInstance()?.repository?.insertAllAssembly(assemblys)

            populateAssemblyList(assemblys)
        }
    }

    fun populateAssemblyList(assemblys: List<Assembly>){

        Handler(Looper.getMainLooper()).post {
            val recycler_adap = AdapterAssembly(assemblys)
            val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(activity)
            recycler.setLayoutManager(layoutManager)
            /*recycler_adap.setOnItemClickListener {
            fun onItemClick(data: Assembly) {
                Toast.makeText(activity, data.name, Toast.LENGTH_SHORT).show()
            }
        }*/
            recycler.setAdapter(recycler_adap)

            loader.visibility = ProgressBar.INVISIBLE
        }
    }

}