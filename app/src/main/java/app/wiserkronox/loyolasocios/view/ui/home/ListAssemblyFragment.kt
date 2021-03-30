package app.wiserkronox.loyolasocios.view.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.wiserkronox.loyolasocios.R
import app.wiserkronox.loyolasocios.service.LoyolaApplication
import app.wiserkronox.loyolasocios.service.model.Assembly
import app.wiserkronox.loyolasocios.service.model.User
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
    private  lateinit var noAssamblys: TextView

    private  lateinit var title_current: TextView
    private  lateinit var date_current: TextView
    private  lateinit var icon_state: ImageView
    private  lateinit var state_user: TextView
    private  lateinit var card_current: CardView

    private  lateinit var currentAssembly: Assembly


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
        noAssamblys = root.findViewById(R.id.text_no_assemblys)
        card_current = root.findViewById(R.id.card_current)

        title_current = root.findViewById(R.id.text_title_current)
        date_current = root.findViewById(R.id.text_date_current)
        icon_state = root.findViewById(R.id.icon_user_state )
        state_user = root.findViewById(R.id.text_user_state)

        if( (activity as HomeActivity).isOnline() ) {
            getUpdateFromServer()
        } else {
            loader.visibility = ProgressBar.INVISIBLE
            noAssamblys.text = "Necesita tener conexion a Internet para ver las asambleas"
            noAssamblys.visibility = TextView.VISIBLE
        }
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
                        loader.visibility = ProgressBar.INVISIBLE
                        if (response.has("assemblys")) {
                            val assemblys = response.getJSONArray("assemblys")
                            if (assemblys.length() > 0) {
                                updateAssemblys(assemblys)
                            } else {
                                noAssamblys.visibility = TextView.VISIBLE
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

            val actives = LoyolaApplication.getInstance()?.repository?.getAllAssemblysStatus("activo")
            val inactives = LoyolaApplication.getInstance()?.repository?.getAllAssemblysStatus("inactivo")

            inactives?.let {
                populateAssemblyList(inactives)
            }

            actives?.let {
                if( it.size >= 1 ){
                    populateCurrentAssembly(it.get(0))
                }
            }

        }
    }

    fun populateCurrentAssembly(current: Assembly){
        Handler(Looper.getMainLooper()).post {
            currentAssembly = current
            title_current.text = currentAssembly.name
            date_current.text = currentAssembly.date

            LoyolaApplication.getInstance()?.user?.let {
                if (it.state_activation == User.STATE_USER_ACTIVE) {
                    icon_state.setImageDrawable(activity?.getDrawable(R.drawable.icon_status_user_active))
                    state_user.text = getString(R.string.text_active_state)
                } else {
                    icon_state.setImageDrawable(activity?.getDrawable(R.drawable.icon_status_user_inactive))
                    state_user.text = getString(R.string.text_inactive_account)
                }
            }
            card_current.visibility = CardView.VISIBLE
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

            noAssamblys.visibility = TextView.INVISIBLE
        }
    }

}