package com.example.aprendeapp.view

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.aprendeapp.R
import com.example.aprendeapp.controller.AuthController
import com.example.aprendeapp.controller.CalificacionController
import com.example.aprendeapp.controller.FavoritoController
import com.example.aprendeapp.controller.RecursoController
import com.example.aprendeapp.databinding.ActivityDetalleRecursoBinding
import com.example.aprendeapp.model.Recurso
import com.example.aprendeapp.util.Constants
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class DetalleRecursoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleRecursoBinding
    private val authController = AuthController.getInstance()
    private val recursoController = RecursoController()
    private val favoritoController = FavoritoController()
    private val calificacionController = CalificacionController()

    private var recursoId: String? = null
    private var recursoActual: Recurso? = null
    private var esFavorito: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleRecursoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        recursoId = intent.getStringExtra(Constants.EXTRA_RECURSO_ID)

        if (recursoId.isNullOrBlank()) {
            Toast.makeText(this, "ID de recurso no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupListeners()
        cargarDetalle()
    }

    private fun setupListeners() {
        binding.btnRegresar.setOnClickListener {
            finish()
        }

        binding.btnFavoritoDetalle.setOnClickListener {
            toggleFavorito()
        }

        binding.btnAbrirEnlace.setOnClickListener {
            recursoActual?.enlace?.let { url ->
                abrirEnlaceEducativo(url)
            }
        }

        binding.btnGuardarCalificacion.setOnClickListener {
            guardarCalificacion()
        }
    }

    private fun cargarDetalle() {
        val id = recursoId ?: return
        val usuario = authController.obtenerUsuarioActual()

        mostrarCargando(true)

        lifecycleScope.launch {
            if (usuario != null) {
                esFavorito = favoritoController.esFavorito(this@DetalleRecursoActivity, usuario.uid, id)
                actualizarIconoFavorito()
            }

            val result = recursoController.obtenerRecursoPorId(id)
            mostrarCargando(false)

            result.onSuccess { recurso ->
                recursoActual = recurso
                mostrarInformacionRecurso(recurso)
            }.onFailure { exception ->
                Toast.makeText(this@DetalleRecursoActivity, exception.localizedMessage ?: "Error al cargar recurso", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun mostrarInformacionRecurso(recurso: Recurso) {
        binding.tvTituloDetalle.text = recurso.titulo
        binding.tvDescripcionDetalle.text = recurso.descripcion
        binding.tvFechaDetalle.text = getString(R.string.fecha_creacion, recurso.fechaCreacion)
        binding.tvTipoDetalle.text = recurso.tipo

        // Badge color
        val (bgColorRes, textColorRes) = when (recurso.tipo) {
            Constants.TIPO_LIBRO -> Pair(R.color.tag_libro_bg, R.color.tag_libro_text)
            Constants.TIPO_VIDEO -> Pair(R.color.tag_video_bg, R.color.tag_video_text)
            Constants.TIPO_ARTICULO -> Pair(R.color.tag_articulo_bg, R.color.tag_articulo_text)
            Constants.TIPO_TUTORIAL -> Pair(R.color.tag_tutorial_bg, R.color.tag_tutorial_text)
            else -> Pair(R.color.primary_light, R.color.primary_dark)
        }

        binding.tvTipoDetalle.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this, bgColorRes))
        binding.tvTipoDetalle.setTextColor(ContextCompat.getColor(this, textColorRes))

        // Image
        if (recurso.imagen.isNotBlank()) {
            binding.imgDetalle.load(recurso.imagen) {
                crossfade(true)
                placeholder(R.drawable.ic_placeholder)
                error(R.drawable.ic_placeholder)
            }
        } else {
            binding.imgDetalle.setImageResource(R.drawable.ic_placeholder)
        }

        // Ratings
        val promedio = recurso.getPromedioCalificaciones()
        val totalVotos = recurso.getTotalCalificaciones()
        val usuario = authController.obtenerUsuarioActual()

        binding.tvPromedioDetalle.text = String.format("%.1f ★", promedio)
        binding.tvTotalVotosDetalle.text = if (totalVotos > 0) "$totalVotos votos" else getString(R.string.sin_calificaciones)

        if (usuario != null) {
            val calificacionUsuario = recurso.getCalificacionUsuario(usuario.uid)
            if (calificacionUsuario > 0) {
                binding.ratingBar.rating = calificacionUsuario.toFloat()
            }
        }
    }

    private fun guardarCalificacion() {
        val recurso = recursoActual ?: return
        val usuario = authController.obtenerUsuarioActual() ?: return
        val puntos = binding.ratingBar.rating.toDouble()

        if (puntos < 1.0) {
            Toast.makeText(this, "Por favor selecciona al menos 1 estrella.", Toast.LENGTH_SHORT).show()
            return
        }

        mostrarCargando(true)

        lifecycleScope.launch {
            val result = calificacionController.calificarRecurso(recurso, usuario.uid, puntos)
            mostrarCargando(false)

            result.onSuccess { recursoActualizado ->
                recursoActual = recursoActualizado
                mostrarInformacionRecurso(recursoActualizado)
                Toast.makeText(this@DetalleRecursoActivity, getString(R.string.calificacion_exitosa), Toast.LENGTH_SHORT).show()
            }.onFailure { ex ->
                Snackbar.make(binding.root, ex.localizedMessage ?: getString(R.string.calificacion_error), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun toggleFavorito() {
        val usuario = authController.obtenerUsuarioActual() ?: return
        val id = recursoId ?: return

        lifecycleScope.launch {
            esFavorito = favoritoController.toggleFavorito(this@DetalleRecursoActivity, usuario.uid, id)
            actualizarIconoFavorito()
            val msg = if (esFavorito) getString(R.string.favorito_agregado) else getString(R.string.favorito_eliminado)
            Toast.makeText(this@DetalleRecursoActivity, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun actualizarIconoFavorito() {
        if (esFavorito) {
            binding.btnFavoritoDetalle.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            binding.btnFavoritoDetalle.setImageResource(R.drawable.ic_favorite_border)
        }
    }

    private fun abrirEnlaceEducativo(url: String) {
        var cleanUrl = url.trim()
        if (!cleanUrl.startsWith("http://") && !cleanUrl.startsWith("https://")) {
            cleanUrl = "https://$cleanUrl"
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cleanUrl))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir el enlace: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun mostrarCargando(cargando: Boolean) {
        binding.progressDetalle.visibility = if (cargando) View.VISIBLE else View.GONE
        binding.btnGuardarCalificacion.isEnabled = !cargando
    }
}
