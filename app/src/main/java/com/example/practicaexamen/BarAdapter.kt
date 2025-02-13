package com.example.practicaexamen

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BarAdapter(private var bars: List<Bar>, private val onItemClick: (Bar) -> Unit) :
    RecyclerView.Adapter<BarAdapter.BarViewHolder>() {

    class BarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.textViewBarName)
        val website: TextView = view.findViewById(R.id.textViewBarWebsite)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bar, parent, false)
        return BarViewHolder(view)
    }

    override fun onBindViewHolder(holder: BarViewHolder, position: Int) {
        val bar = bars[position]
        holder.name.text = bar.name
        holder.website.text = bar.website
        holder.website.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(bar.website))
            it.context.startActivity(intent)
        }
        holder.itemView.setOnClickListener { onItemClick(bar) }
    }

    override fun getItemCount(): Int = bars.size

    // Método para actualizar la lista de bares dinámicamente
    fun updateData(newBars: List<Bar>) {
        bars = newBars
        notifyDataSetChanged()
    }
}
