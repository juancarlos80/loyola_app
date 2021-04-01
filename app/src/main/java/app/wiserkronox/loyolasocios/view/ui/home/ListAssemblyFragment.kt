package app.wiserkronox.loyolasocios.view.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
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
import app.wiserkronox.loyolasocios.view.callback.ListAssemblyCallback
import app.wiserkronox.loyolasocios.view.ui.HomeActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class ListAssemblyFragment : Fragment(), ListAssemblyCallback {

    private  lateinit var loader: ProgressBar
    private  lateinit var recycler: RecyclerView
    private  lateinit var noAssamblys: TextView

    private  lateinit var title_current: TextView
    private  lateinit var date_current: TextView
    private  lateinit var icon_state: ImageView
    private  lateinit var state_user: TextView
    private  lateinit var card_current: CardView
    private  lateinit var card_journey: CardView
    private  lateinit var btn_zoom: Button
    private  lateinit var image_journey: ImageView
    private  lateinit var image_memorys: ImageView
    private  lateinit var text_title_attach: TextView
    private  lateinit var webview_journey: WebView
    private  lateinit var loader_webview: ProgressBar

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
        card_journey = root.findViewById(R.id.card_journey)
        image_journey = root.findViewById(R.id.image_journey)
        text_title_attach = root.findViewById(R.id.text_title_attach)
        webview_journey = root.findViewById(R.id.webview_journey)
        loader_webview = root.findViewById(R.id.progress_webview)

        image_memorys = root.findViewById(R.id.image_memorys)

        title_current = root.findViewById(R.id.text_title_current)
        date_current = root.findViewById(R.id.text_date_current)
        icon_state = root.findViewById(R.id.icon_user_state )
        state_user = root.findViewById(R.id.text_user_state)
        btn_zoom = root.findViewById(R.id.btn_zoom)

        noAssamblys.visibility = TextView.INVISIBLE
        card_current.visibility = CardView.GONE

        LoyolaApplication.getInstance()?.user?.let {user ->
            btn_zoom.setOnClickListener{
                if( user.state_activation == User.STATE_USER_ACTIVE ){
                    (activity as HomeActivity).joinMeeting(
                        currentAssembly.zoom_code,
                        currentAssembly.zoom_password,
                        user.names+" "+user.last_name_1+" "+user.last_name_2)
                } else {
                    (activity as HomeActivity).showMessage("Solo socios con cuenta activa pueden ingresar a la asamblea")
                }
            }
        }

        webview_journey.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                loader_webview.visibility = ProgressBar.GONE
                webview_journey.visibility = WebView.VISIBLE
            }
        }

        text_title_attach.setOnClickListener {
            recycler.visibility = RecyclerView.VISIBLE
            card_journey.visibility = CardView.GONE
        }

        image_journey.setOnClickListener {
            recycler.visibility = RecyclerView.GONE
            text_title_attach.text = "Orden del día"
            webview_journey.loadData(currentAssembly.journey, "text/html; charset=utf-8", "UTF-8");
            card_journey.visibility = CardView.VISIBLE
        }

        image_memorys.setOnClickListener {
            openMemorys(currentAssembly)
        }


        if( (activity as HomeActivity).isOnline() ) {
            getUpdateFromServer()
        } else {
            cant_load_assemblys("Necesita tener conexión a Internet para ver las asambleas")
        }
        return root
    }

    fun cant_load_assemblys(message:String){
        loader.visibility = ProgressBar.INVISIBLE
        noAssamblys.text = message
        noAssamblys.visibility = TextView.VISIBLE
        card_current.visibility = CardView.GONE
    }

    override fun openMemorys( assembly: Assembly ){
        if( assembly.memory == ""){
            (activity as HomeActivity).showMessage("Aún no se definieron las memorias")
        } else {
            recycler.visibility = RecyclerView.GONE
            text_title_attach.text = "Memorias"
            //webview_journey.webViewClient = WebViewClient()
            webview_journey.settings.setSupportZoom(true)
            webview_journey.settings.javaScriptEnabled = true

            webview_journey.loadUrl("https://docs.google.com/gview?embedded=true&url=" + assembly.memory)
            card_journey.visibility = CardView.VISIBLE
        }
    }

    override fun openStatements(assembly: Assembly){
        if( assembly.statements == ""){
            (activity as HomeActivity).showMessage("Aún no se compartieron los estados")
        } else {
            recycler.visibility = RecyclerView.GONE
            text_title_attach.text = "Estado de resultados"
            webview_journey.settings.setSupportZoom(true)
            webview_journey.settings.javaScriptEnabled = true

            webview_journey.loadUrl("https://docs.google.com/gview?embedded=true&url=" + assembly.statements)
            card_journey.visibility = CardView.VISIBLE
        }
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
                                val assemblyRest = AssemblyRest(it)
                                insertAssemblys( assemblyRest.getAssemblysList( assemblys) )
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
                    cant_load_assemblys("Error de conexión con el servidor")
                }
            )

            // Add the request to the RequestQueue.
            LoyolaService.getInstance(it).addToRequestQueue(jsonObjectRequest)
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
                if(it.isNotEmpty()){
                    populateCurrentAssembly(it[0])
                }
            }

        }
    }

    fun populateCurrentAssembly(current: Assembly){
        currentAssembly = current

        Handler(Looper.getMainLooper()).post {

            title_current.text = currentAssembly.name
            date_current.text = currentAssembly.date

            LoyolaApplication.getInstance()?.user?.let { user ->
                if (user.state_activation == User.STATE_USER_ACTIVE) {
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
            val recycler_adap = AdapterAssembly(assemblys, this)
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