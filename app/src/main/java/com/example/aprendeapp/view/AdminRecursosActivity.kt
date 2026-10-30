package com.example.aprendeapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aprendeapp.R
import com.example.aprendeapp.adapter.RecursoAdapter
import com.example.aprendeapp.controller.AuthController
import com.example.aprendeapp.controller.FavoritoController
import com.example.aprendeapp.controller.RecursoController
import com.example.aprendeapp.databinding.ActivityAdminRecursosBinding
import com.example.aprendeapp.model.Recurso
import com.example.aprendeapp.util.Constants
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AdminRecursosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminRecursosBinding
    private val authController = AuthController.getInstance()
    private val recursoController = RecursoController()
    private val favoritoController = FavoritoController()

    private lateinit var recursoAdapter: RecursoAdapter
    private var listaRecursos: List<Recurso> = emptyList()
    private var setIdsFavoritos: Set<String> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val usuario = authController.obtenerUsuarioActual()
        if (usuario == null || !usuario.esDocente) {
            Toast.makeText(this, "Acceso exclusivo para Docentes autorizados", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        binding = ActivityAdminRecursosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        configurarRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        cargarCatalogo()
    }

    private fun setupListeners() {
        binding.btnRegresarAdmin.setOnClickListener {
            finish()
        }

        binding.fabAgregarRecurso.setOnClickListener {
            startActivity(Intent(this, FormularioRecursoActivity::class.java))
        }

        binding.swipeRefreshAdmin.setOnRefreshListener {
            cargarCatalogo()
        }
    }

    private fun configurarRecyclerView() {
        recursoAdapter = RecursoAdapter(
            context = this,
            listaRecursos = emptyList(),
            idsFavoritos = setIdsFavoritos,
            esModoAdmin = true,
            onDetallesClick = { recurso ->
                val intent = Intent(this, DetalleRecursoActivity::class.java)
                intent.putExtra(Constants.EXTRA_RECURSO_ID, recurso.id)
                startActivity(intent)
            },
            onFavoritoToggle = { recurso ->
                recurso.id?.let { id ->
                    val usuario = authController.obtenerUsuarioActual()
                    usuario?.uid?.let { uid ->
                        toggleFavorito(uid, id)
                    }
                }
            },
            onEditarClick = { recurso ->
                val intent = Intent(this, FormularioRecursoActivity::class.java)
                intent.putExtra(Constants.EXTRA_RECURSO_EDIT, recurso)
                startActivity(intent)
            },
            onEliminarClick = { recurso ->
                confirmarEliminacion(recurso)
            }
        )

        binding.rvAdminRecursos.layoutManager = LinearLayoutManager(this)
        binding.rvAdminRecursos.adapter = recursoAdapter
    }

    private fun cargarCatalogo() {
        binding.progressAdmin.visibility = View.VISIBLE

        lifecycleScope.launch {
            val usuario = authController.obtenerUsuarioActual()
            if (usuario != null) {
                val favoritos = favoritoController.obtenerFavoritosUsuario(this@AdminRecursosActivity, usuario.uid)
                setIdsFavoritos = favoritos.map { it.recursoId }.toSet()
            }

            val result = recursoController.obtenerRecursos()
            binding.progressAdmin.visibility = View.GONE
            binding.swipeRefreshAdmin.isRefreshing = false

            result.onSuccess { recursos ->
                listaRecursos = recursos
                recursoAdapter.actualizarDatos(listaRecursos, setIdsFavoritos)
            }.onFailure { ex ->
                Snackbar.make(binding.root, ex.localizedMessage ?: "Error al cargar recursos", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun confirmarEliminacion(recurso: Recurso) {
        val recursoId = recurso.id ?: return

        AlertDialog.Builder(this)
            .setTitle(R.string.confirmacion_eliminar_titulo)
            .setMessage(R.string.confirmacion_eliminar_msg)
            .setPositiveButton(R.string.eliminar) { dialog, _ ->
                eliminarRecurso(recursoId)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancelar) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun eliminarRecurso(recursoId: String) {
        binding.progressAdmin.visibility = View.VISIBLE

        lifecycleScope.launch {
            val result = recursoController.eliminarRecurso(recursoId)
            binding.progressAdmin.visibility = View.GONE

            result.onSuccess {
                Toast.makeText(this@AdminRecursosActivity, getString(R.string.recurso_eliminado_exito), Toast.LENGTH_SHORT).show()
                cargarCatalogo()
            }.onFailure { ex ->
                Snackbar.make(binding.root, ex.localizedMessage ?: "Error al eliminar el recurso", Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun toggleFavorito(usuarioId: String, recursoId: String) {
        lifecycleScope.launch {
            favoritoController.toggleFavorito(this@AdminRecursosActivity, usuarioId, recursoId)
            val favs = favoritoController.obtenerFavoritosUsuario(this@AdminRecursosActivity, usuarioId)
            setIdsFavoritos = favs.map { it.recursoId }.toSet()
            recursoAdapter.actualizarFavoritos(setIdsFavoritos)
        }
    }
}
