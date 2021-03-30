package app.wiserkronox.loyolasocios.view.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.wiserkronox.loyolasocios.R
import app.wiserkronox.loyolasocios.service.model.Assembly


class AdapterAssembly internal constructor(private val assemblyList: List<Assembly>) :
    RecyclerView.Adapter<AdapterAssembly.AssemblyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssemblyViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.adapter_assembly, parent, false)
        return AssemblyViewHolder(view)
    }

    override fun onBindViewHolder(holder: AssemblyViewHolder, position: Int) {
        val assembly = assemblyList[position]
        holder.name.text = assembly.name
        holder.date.text = assembly.date
    }

    override fun getItemCount(): Int {
        return assemblyList.size
    }

    class AssemblyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.text_date)
        val name: TextView = itemView.findViewById(R.id.text_name)
    }
}