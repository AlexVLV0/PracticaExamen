package com.example.practicaexamen


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class FragmentBarDetail : Fragment() {

    private lateinit var dbHelper: SQLiteHelper
    private var barId: Int = 0
    private var barRating: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_bar_detail, container, false)

        dbHelper = SQLiteHelper(requireContext())

        val name = arguments?.getString("name")
        val address = arguments?.getString("address")
        val latitude = arguments?.getDouble("latitude")
        val longitude = arguments?.getDouble("longitude")
        val website = arguments?.getString("website")
        barRating = arguments?.getFloat("rating") ?: 0f

        rootView.findViewById<RatingBar>(R.id.ratingBar).rating = barRating

        // Iniciar el mapa con la latitud y longitud
        val mapView = rootView.findViewById<MapView>(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { googleMap ->
            val location = LatLng(latitude ?: 0.0, longitude ?: 0.0)
            googleMap.addMarker(MarkerOptions().position(location).title(name))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }

        rootView.findViewById<Button>(R.id.buttonEditBar).setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
            val input = EditText(requireContext())
            input.setText(name)
            dialog.setView(input)
            dialog.setPositiveButton("Guardar") { _, _ ->
                val newName = input.text.toString()
                val updatedBar = Bar(barId, newName, address ?: "", latitude ?: 0.0, longitude ?: 0.0, website ?: "", barRating)
                dbHelper.updateBar(updatedBar)
            }
            dialog.setNegativeButton("Cancelar", null)
            dialog.show()
        }

        rootView.findViewById<Button>(R.id.buttonDeleteBar).setOnClickListener {
            dbHelper.deleteBar(barId)
            fragmentManager?.popBackStack()

        }

        return rootView
    }

    override fun onResume() {
        super.onResume()
        val args = arguments
        barId = args?.getInt("id") ?: 0
    }

   
}
