package com.example.practicaexamen

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar los fragments al inicio
        if (savedInstanceState == null) {
            val fragment = FragmentBarCreateAndList()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_list, fragment)
                .commit()
        }
    }
}