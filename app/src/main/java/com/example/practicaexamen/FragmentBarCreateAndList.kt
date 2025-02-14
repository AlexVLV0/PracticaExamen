package com.example.practicaexamen

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import android.Manifest


class FragmentBarCreateAndList : Fragment() {

    private lateinit var dbHelper: SQLiteHelper
    private lateinit var listView: ListView
    private lateinit var barsList: MutableList<Bar>
    private lateinit var adapter: SimpleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_bar_create_and_list, container, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("FragmentBarCreateAndList", "Solicitando permiso de notificación...")
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            } else {
                Log.d("FragmentBarCreateAndList", "Permiso de notificación ya concedido")
            }
        }
        dbHelper = SQLiteHelper(requireContext())
        listView = rootView.findViewById(R.id.listViewBares)
        val buttonSaveBar = rootView.findViewById<Button>(R.id.buttonSaveBar)
        val buttonShowBars = rootView.findViewById<Button>(R.id.buttonShowBars)

        // Inicializar lista de bares
        barsList = dbHelper.getAllBars().toMutableList()
        updateBarList() // Cargar la lista desde el inicio


        // Botón para guardar un nuevo bar
        buttonSaveBar.setOnClickListener {
            val name = rootView.findViewById<EditText>(R.id.editTextName).text.toString()
            val address = rootView.findViewById<EditText>(R.id.editTextAddress).text.toString()
            val latitude =
                rootView.findViewById<EditText>(R.id.editTextLatitude).text.toString()
                    .toDoubleOrNull() ?: 0.0
            val longitude =
                rootView.findViewById<EditText>(R.id.editTextLongitude).text.toString()
                    .toDoubleOrNull() ?: 0.0
            val website = rootView.findViewById<EditText>(R.id.editTextWebsite).text.toString()
            val rating = rootView.findViewById<RatingBar>(R.id.ratingBarInput).rating

            if (name.isNotEmpty() && website.isNotEmpty()) {
                val newBar = Bar(0, name, address, latitude, longitude, website, rating)
                dbHelper.insertBar(newBar)
                updateBarList()
                val notificationHelper = context?.let { it1 -> NotificationHelper(it1) }
                if (notificationHelper != null) {
                    notificationHelper.showNewBarNotification(name, website)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Nombre y página web son obligatorios",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Botón para mostrar los bares guardados
        buttonShowBars.setOnClickListener {
            updateBarList()
        }

        // Evento al hacer clic en un bar
        listView.setOnItemClickListener { _, view, position, _ ->
            val selectedBar = barsList[position]

            // Si se hace clic en el enlace, abrir en el navegador
            val websiteTextView = view.findViewById<TextView>(R.id.textViewBarWebsite)
            websiteTextView.setOnClickListener {
                val website = selectedBar.website?.trim() // Elimina espacios extra

                if (!website.isNullOrEmpty()) {
                    val fixedUrl =
                        if (website.startsWith("http://") || website.startsWith("https://")) {
                            website
                        } else {
                            "https://$website" // Añade "https://" si falta
                        }

                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fixedUrl))

                        // Crear un chooser para que el usuario elija una app
                        val chooser = Intent.createChooser(intent, "Abrir enlace con...")

                        if (intent.resolveActivity(requireActivity().packageManager) != null) {
                            startActivity(chooser)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "No hay navegador disponible",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            requireContext(),
                            "Error al abrir la URL",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "URL no válida", Toast.LENGTH_SHORT).show()
                }
            }


            // Si se hace clic en otro lado, abrir los detalles del bar
            openBarDetail(selectedBar)
        }

        return rootView
    }

    // Función para actualizar la lista de bares en el ListView
    fun updateBarList() {
        barsList = dbHelper.getAllBars().toMutableList()

        // Crear lista de mapas con el nombre y el enlace web
        val dataList = barsList.map { bar ->
            mapOf("name" to bar.name, "website" to bar.website)
        }

        // Adaptador para personalizar el ListView
        adapter = SimpleAdapter(
            requireContext(),
            dataList,
            R.layout.item_bar,
            arrayOf("name", "website"),
            intArrayOf(R.id.textViewBarName, R.id.textViewBarWebsite)
        )

        listView.adapter = adapter
    }

    // Función para abrir el detalle de un bar seleccionado
    private fun openBarDetail(bar: Bar) {
        val fragment = FragmentBarDetail()
        val bundle = Bundle().apply {
            putInt("id", bar.id)
            putString("name", bar.name)
            putString("address", bar.address)
            putDouble("latitude", bar.latitude)
            putDouble("longitude", bar.longitude)
            putString("website", bar.website)
            putFloat("rating", bar.rating)
        }
        fragment.arguments = bundle
        fragmentManager?.beginTransaction()
            ?.replace(R.id.fragment_container_detail, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) { // Código de solicitud
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("FragmentBarCreateAndList", "Permiso de notificación concedido")
            } else {
                Log.e("FragmentBarCreateAndList", "Permiso de notificación denegado")
                Toast.makeText(
                    requireContext(),
                    "Se requieren permisos para notificaciones",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}