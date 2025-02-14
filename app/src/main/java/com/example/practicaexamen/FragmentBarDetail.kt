package com.example.practicaexamen

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class FragmentBarDetail : Fragment(), OnMapReadyCallback {

    private lateinit var dbHelper: SQLiteHelper
    private var barId: Int = 0
    private lateinit var mapView: MapView
    private var googleMap: GoogleMap? = null
    private lateinit var ratingBar: RatingBar
    private lateinit var textViewName: TextView
    private lateinit var textViewWebsite: TextView
    private lateinit var buttonEditBar: Button
    private lateinit var buttonDeleteBar: Button
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var rating: Float = 0f

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_bar_detail, container, false)

        // Inicializar vistas correctamente
        textViewName = rootView.findViewById(R.id.textViewName)
        textViewWebsite = rootView.findViewById(R.id.textViewWebsite)
        ratingBar = rootView.findViewById(R.id.ratingBar)
        mapView = rootView.findViewById(R.id.mapView)
        buttonEditBar = rootView.findViewById(R.id.buttonEditBar)
        buttonDeleteBar = rootView.findViewById(R.id.buttonDeleteBar)

        // Inicializar base de datos
        dbHelper = SQLiteHelper(requireContext())

        // Verificar si hay argumentos antes de acceder a ellos
        arguments?.let {
            barId = it.getInt("id", 0)
            val name = it.getString("name", "")
            latitude = it.getDouble("latitude", 0.0)
            longitude = it.getDouble("longitude", 0.0)
            val website = it.getString("website", "")
            rating = it.getFloat("rating", 0f)

            // Asignar valores
            textViewName.text = name
            textViewWebsite.text = website
            ratingBar.rating = rating
        }

        // Hacer que el enlace web abra el navegador
        textViewWebsite.setOnClickListener {
            val website = textViewWebsite.text.toString().trim() // Elimina espacios en blanco

            if (website.isNotEmpty()) {
                val fixedUrl = if (website.startsWith("http://") || website.startsWith("https://")) {
                    website
                } else {
                    "https://$website"
                }

                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fixedUrl)).apply {
                        addCategory(Intent.CATEGORY_BROWSABLE) // Asegura que solo se muestren navegadores
                    }
                    val chooser = Intent.createChooser(intent, "Abrir enlace con...")

                    startActivity(chooser)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error al abrir la URL: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "URL no válida", Toast.LENGTH_SHORT).show()
            }
        }




        buttonEditBar.setOnClickListener {
            showEditDialog()
        }

        buttonDeleteBar.setOnClickListener {
            dbHelper.deleteBar(barId)
            updateListView()
            fragmentManager?.popBackStack()
        }

        // Inicializar el mapa
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        return rootView
    }

    private fun showEditDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_bar, null)
        val editTextName = dialogView.findViewById<EditText>(R.id.editTextEditName)
        val editTextLatitude = dialogView.findViewById<EditText>(R.id.editTextEditLatitude)
        val editTextLongitude = dialogView.findViewById<EditText>(R.id.editTextEditLongitude)
        val editTextWebsite = dialogView.findViewById<EditText>(R.id.editTextEditWebsite)
        val ratingBarEdit = dialogView.findViewById<RatingBar>(R.id.ratingBarEdit)

        // Establecer valores actuales
        editTextName.setText(textViewName.text.toString())
        editTextLatitude.setText(latitude.toString())
        editTextLongitude.setText(longitude.toString())
        editTextWebsite.setText(textViewWebsite.text.toString())
        ratingBarEdit.rating = rating

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Editar Bar")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val newName = editTextName.text.toString()
                val newLatitude = editTextLatitude.text.toString().toDoubleOrNull() ?: 0.0
                val newLongitude = editTextLongitude.text.toString().toDoubleOrNull() ?: 0.0
                val newWebsite = editTextWebsite.text.toString()
                val newRating = ratingBarEdit.rating

                // Actualizar en la base de datos
                val updatedBar = Bar(barId, newName, "", newLatitude, newLongitude, newWebsite, newRating)
                dbHelper.updateBar(updatedBar)

                // Refrescar en el fragmento de detalle
                textViewName.text = newName
                textViewWebsite.text = newWebsite
                ratingBar.rating = newRating
                latitude = newLatitude
                longitude = newLongitude

                // Actualizar el ListView en el otro fragment
                updateListView()

                // Actualizar el mapa si cambian latitud y longitud
                googleMap?.clear()
                val location = LatLng(newLatitude, newLongitude)
                googleMap?.addMarker(MarkerOptions().position(location).title(newName))
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.show()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val location = LatLng(latitude, longitude)
        val marker = googleMap?.addMarker(MarkerOptions().position(location).title(textViewName.text.toString()))
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

        // Listener para abrir la web al hacer clic en el marcador
        googleMap?.setOnMarkerClickListener { marker ->
            val website = textViewWebsite.text.toString().trim() // Elimina espacios en blanco

            if (website.isNotEmpty()) {
                val fixedUrl = if (website.startsWith("http://") || website.startsWith("https://")) {
                    website
                } else {
                    "https://$website"
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fixedUrl)).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE) // Asegura que solo se muestren navegadores
                }
                val chooser = Intent.createChooser(intent, "Abrir enlace con...")
                startActivity(chooser)
            } else {
                Toast.makeText(requireContext(), "No hay página web válida", Toast.LENGTH_SHORT).show()
            }
            true  // Devuelve true para que el evento se maneje aquí y no se haga el zoom
        }
    }


    private fun updateListView() {
        val fragment = fragmentManager?.findFragmentById(R.id.fragment_container_list) as? FragmentBarCreateAndList
        fragment?.updateBarList()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}