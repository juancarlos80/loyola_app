package app.wiserkronox.loyolasocios.view.callback

import app.wiserkronox.loyolasocios.service.model.Assembly

interface ListAssemblyCallback {
    fun openStatements(assembly: Assembly)
    fun openMemorys(assembly: Assembly)
}