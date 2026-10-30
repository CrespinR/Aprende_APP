package com.example.aprendeapp.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.aprendeapp.R
import com.example.aprendeapp.databinding.ItemRecursoBinding
import com.example.aprendeapp.model.Recurso
import com.example.aprendeapp.util.Constants

class RecursoAdapter(
    private val context: Context,
    private var listaRecursos: List<Recurso>,
    private var idsFavoritos: Set<String> = emptySet(),
    private val esModoAdmin: Boolean = false,
    private val onDetallesClick: (Recurso) -> Unit,
    private val onFavoritoToggle: (Recurso) -> Unit,
    private val onEditarClick: ((Recurso) -> Unit)? = null,
    private val onEliminarClick: ((Recurso) -> Unit)? = null
) : RecyclerView.Adapter<RecursoAdapter.RecursoViewHolder>() {

    fun actualizarDatos(nuevaLista: List<Recurso>, nuevosFavoritos: Set<String> = idsFavoritos) {
        listaRecursos = nuevaLista
        idsFavoritos = nuevosFavoritos
        notifyDataSetChanged()
    }

    fun actualizarFavoritos(nuevosFavoritos: Set<String>) {
        idsFavoritos = nuevosFavoritos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecursoViewHolder {
        val binding = ItemRecursoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecursoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecursoViewHolder, position: Int) {
        holder.bind(listaRecursos[position])
    }

    override fun getItemCount(): Int = listaRecursos.size

    inner class RecursoViewHolder(private val binding: ItemRecursoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(recurso: Recurso) {
            binding.tvTitulo.text = recurso.titulo
            binding.tvDescripcion.text = recurso.descripcion
            binding.tvFecha.text = recurso.fechaCreacion.takeIf { it.isNotBlank() } ?: ""

            // Cargar imagen con Coil
            if (recurso.imagen.isNotBlank()) {
                binding.imgRecurso.load(recurso.imagen) {
                    crossfade(true)
                    placeholder(R.drawable.ic_placeholder)
                    error(R.drawable.ic_placeholder)
                }
            } else {
                binding.imgRecurso.setImageResource(R.drawable.ic_placeholder)
            }

            // Configurar etiqueta de tipo
            binding.tvTipoBadge.text = recurso.tipo
            configurarBadgeTipo(recurso.tipo)

            // Rating
            val promedio = recurso.getPromedioCalificaciones()
            val totalVotos = recurso.getTotalCalificaciones()
            binding.tvRatingPromedio.text = String.format("%.1f", promedio)
            binding.tvTotalVotos.text = if (totalVotos > 0) "($totalVotos)" else context.getString(R.string.sin_calificaciones)

            // Favorito estado
            val esFav = recurso.id != null && idsFavoritos.contains(recurso.id)
            if (esFav) {
                binding.btnFavorito.setImageResource(R.drawable.ic_favorite_filled)
            } else {
                binding.btnFavorito.setImageResource(R.drawable.ic_favorite_border)
            }

            // Click Listeners
            binding.btnFavorito.setOnClickListener { onFavoritoToggle(recurso) }
            binding.btnDetalles.setOnClickListener { onDetallesClick(recurso) }
            binding.root.setOnClickListener { onDetallesClick(recurso) }

            // Botones de administración
            if (esModoAdmin) {
                binding.btnEditarAdmin.visibility = View.VISIBLE
                binding.btnEliminarAdmin.visibility = View.VISIBLE
                binding.btnEditarAdmin.setOnClickListener { onEditarClick?.invoke(recurso) }
                binding.btnEliminarAdmin.setOnClickListener { onEliminarClick?.invoke(recurso) }
            } else {
                binding.btnEditarAdmin.visibility = View.GONE
                binding.btnEliminarAdmin.visibility = View.GONE
            }
        }

        private fun configurarBadgeTipo(tipo: String) {
            val (bgColorRes, textColorRes) = when (tipo) {
                Constants.TIPO_LIBRO -> Pair(R.color.tag_libro_bg, R.color.tag_libro_text)
                Constants.TIPO_VIDEO -> Pair(R.color.tag_video_bg, R.color.tag_video_text)
                Constants.TIPO_ARTICULO -> Pair(R.color.tag_articulo_bg, R.color.tag_articulo_text)
                Constants.TIPO_TUTORIAL -> Pair(R.color.tag_tutorial_bg, R.color.tag_tutorial_text)
                else -> Pair(R.color.primary_light, R.color.primary_dark)
            }

            val bgColor = ContextCompat.getColor(context, bgColorRes)
            val textColor = ContextCompat.getColor(context, textColorRes)

            binding.tvTipoBadge.backgroundTintList = ColorStateList.valueOf(bgColor)
            binding.tvTipoBadge.setTextColor(textColor)
        }
    }
}
