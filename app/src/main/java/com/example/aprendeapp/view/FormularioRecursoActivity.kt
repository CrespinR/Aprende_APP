package com.example.aprendeapp.view

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aprendeapp.R
import com.example.aprendeapp.controller.AuthController
import com.example.aprendeapp.controller.RecursoController
import com.example.aprendeapp.databinding.ActivityFormularioRecursoBinding
import com.example.aprendeapp.model.Recurso
import com.example.aprendeapp.util.Constants
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FormularioRecursoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFormularioRecursoBinding
    private val authController = AuthController.getInstance()
    private val recursoController = RecursoController()

    private var recursoEditar: Recurso? = null
    private var esModoEdicion: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val usuario = authController.obtenerUsuarioActual()
        if (usuario == null || !usuario.esDocente) {
            Toast.makeText(this, "Acceso exclusivo para Docentes", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding = ActivityFormularioRecursoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        recursoEditar = intent.getSerializableExtra(Constants.EXTRA_RECURSO_EDIT) as? Recurso
        esModoEdicion = recursoEditar != null

        configurarDropdownTipos()
        configurarUI()
        setupListeners()
    }

    private fun configurarDropdownTipos() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            Constants.TIPOS_PERMITIDOS
        )
        binding.actTipo.setAdapter(adapter)
    }

    private fun configurarUI() {
        if (esModoEdicion) {
            val rec = recursoEditar!!
            binding.tvTituloFormularioHeader.text = getString(R.string.titulo_editar_recurso)
            binding.etTitulo.setText(rec.titulo)
            binding.etDescripcion.setText(rec.descripcion)
            binding.actTipo.setText(rec.tipo, false)
            binding.etEnlace.setText(rec.enlace)
            binding.etImagenUrl.setText(rec.imagen)
        } else {
            binding.tvTituloFormularioHeader.text = getString(R.string.titulo_crear_recurso)
            binding.actTipo.setText(Constants.TIPO_LIBRO, false)
        }
    }

    private fun setupListeners() {
        binding.btnRegresarFormulario.setOnClickListener {
            finish()
        }

        binding.btnGuardarRecurso.setOnClickListener {
            guardarRecurso()
        }
    }

    private fun guardarRecurso() {
        val titulo = binding.etTitulo.text?.toString()?.trim() ?: ""
        val descripcion = binding.etDescripcion.text?.toString()?.trim() ?: ""
        val tipo = binding.actTipo.text?.toString()?.trim() ?: ""
        val enlace = binding.etEnlace.text?.toString()?.trim() ?: ""
        val imagen = binding.etImagenUrl.text?.toString()?.trim() ?: ""

        // Limpiar errores
        binding.tilTitulo.error = null
        binding.tilDescripcion.error = null
        binding.tilTipo.error = null
        binding.tilEnlace.error = null

        var hayError = false

        if (titulo.isBlank()) {
            binding.tilTitulo.error = getString(R.string.campo_requerido)
            hayError = true
        }

        if (descripcion.isBlank()) {
            binding.tilDescripcion.error = getString(R.string.campo_requerido)
            hayError = true
        }

        if (tipo.isBlank() || !Constants.TIPOS_PERMITIDOS.contains(tipo)) {
            binding.tilTipo.error = "Seleccione un tipo válido"
            hayError = true
        }

        if (enlace.isBlank()) {
            binding.tilEnlace.error = getString(R.string.campo_requerido)
            hayError = true
        }

        if (hayError) return

        mostrarCargando(true)

        lifecycleScope.launch {
            if (esModoEdicion) {
                val rec = recursoEditar!!
                val id = rec.id ?: ""
                val recursoActualizado = rec.copy(
                    titulo = titulo,
                    descripcion = descripcion,
                    tipo = tipo,
                    enlace = enlace,
                    imagen = imagen
                )

                val result = recursoController.actualizarRecurso(id, recursoActualizado)
                mostrarCargando(false)

                result.onSuccess {
                    Toast.makeText(this@FormularioRecursoActivity, getString(R.string.recurso_actualizado_exito), Toast.LENGTH_SHORT).show()
                    finish()
                }.onFailure { ex ->
                    Snackbar.make(binding.root, ex.localizedMessage ?: "Error al actualizar recurso", Snackbar.LENGTH_LONG).show()
                }
            } else {
                val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val nuevoRecurso = Recurso(
                    titulo = titulo,
                    descripcion = descripcion,
                    tipo = tipo,
                    enlace = enlace,
                    imagen = imagen,
                    fechaCreacion = fechaHoy
                )

                val result = recursoController.crearRecurso(nuevoRecurso)
                mostrarCargando(false)

                result.onSuccess {
                    Toast.makeText(this@FormularioRecursoActivity, getString(R.string.recurso_creado_exito), Toast.LENGTH_SHORT).show()
                    finish()
                }.onFailure { ex ->
                    Snackbar.make(binding.root, ex.localizedMessage ?: "Error al crear recurso", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun mostrarCargando(cargando: Boolean) {
        binding.progressFormulario.visibility = if (cargando) View.VISIBLE else View.GONE
        binding.btnGuardarRecurso.isEnabled = !cargando
    }
}
