package app.wiserkronox.loyolasocios.view.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import app.wiserkronox.loyolasocios.R
import app.wiserkronox.loyolasocios.lifecycle.MainObserver

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        if( savedInstanceState == null ){
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<WithoutSessionFragment>(R.id.fragment_container_view)
            }
        }

        lifecycle.addObserver( MainObserver() )

    }

    fun goManualRegister( ){
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<ManualRegisterFragment>(R.id.fragment_container_view)
        }
    }

    fun goWithoutSession( ){
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace<WithoutSessionFragment>(R.id.fragment_container_view)
        }
    }
}