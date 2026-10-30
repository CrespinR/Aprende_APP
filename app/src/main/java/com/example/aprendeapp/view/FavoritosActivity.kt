package com.example.aprendeapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.aprendeapp.R
import com.example.aprendeapp.adapter.RecursoAdapter
import com.example.aprendeapp.controller.AuthController
import com.example.aprendeapp.controller.FavoritoController
import com.example.aprendeapp.controller.RecursoController
import com.example.aprendeapp.databinding.ActivityFavoritosBinding
import com.example.aprendeapp.model.Recurso
import com.example.aprendeapp.util.Constants
import kotlinx.coroutines.launch

class FavoritosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritosBinding
    private val authController = AuthController.getInstance()
    private val recursoController = RecursoController()
    private val favoritoController = FavoritoController()

    private lateinit var recursoAdapter: RecursoAdapter
    private var listaFavoritos: MutableList<Recurso> = mutableListOf()
    private var setIdsFavoritos: MutableSet<String> = mutableSetOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegresarFavoritos.setOnClickListener {
            finish()
        }

        binding.swipeRefreshFavoritos.setOnRefreshListener {
            cargarFavoritos()
        }

        configurarRecyclerView()
        cargarFavoritos()
    }

    private fun configurarRecyclerView() {
        recursoAdapter = RecursoAdapter(
            context = this,
            listaRecursos = listaFavoritos,
            idsFavoritos = setIdsFavoritos,
            esModoAdmin = false,
            onDetallesClick = { recurso ->
                val intent = Intent(this, DetalleRecursoActivity::class.java)
                intent.putExtra(Constants.EXTRA_RECURSO_ID, recurso.id)
                startActivity(intent)
            },
            onFavoritoToggle = { recurso ->
                recurso.id?.let { id ->
                    toggleFavorito(id)
                }
            }
        )

        binding.rvFavoritos.layoutManager = LinearLayoutManager(this)
        binding.rvFavoritos.adapter = recursoAdapter
    }

    private fun cargarFavoritos() {
        val usuario = authController.obtenerUsuarioActual()
        if (usuario == null) {
            finish()
            return
        }

        binding.progressFavoritos.visibility = View.VISIBLE
        binding.layoutVacioFavoritos.visibility = View.GONE

        lifecycleScope.launch {
            val result = recursoController.obtenerRecursos()
            binding.progressFavoritos.visibility = View.GONE
            binding.swipeRefreshFavoritos.isRefreshing = false

            result.onSuccess { todosRecursos ->
                val recursosFav = favoritoController.filtrarRecursosFavoritos(
                    this@FavoritosActivity,
                    usuario.uid,
                    todosRecursos
                )
                listaFavoritos = recursosFav.toMutableList()
                setIdsFavoritos = recursosFav.mapNotNull { it.id }.toMutableSet()

                recursoAdapter.actualizarDatos(listaFavoritos, setIdsFavoritos)

                if (listaFavoritos.isEmpty()) {
                    binding.layoutVacioFavoritos.visibility = View.VISIBLE
                } else {
                    binding.layoutVacioFavoritos.visibility = View.GONE
                }
            }.onFailure { ex ->
                Toast.makeText(this@FavoritosActivity, ex.localizedMessage ?: "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleFavorito(recursoId: String) {
        val usuario = authController.obtenerUsuarioActual() ?: return

        lifecycleScope.launch {
            favoritoController.toggleFavorito(this@FavoritosActivity, usuario.uid, recursoId)
            Toast.makeText(this@FavoritosActivity, getString(R.string.favorito_eliminado), Toast.LENGTH_SHORT).show()

            listaFavoritos.removeAll { it.id == recursoId }
            setIdsFavoritos.remove(recursoId)

            recursoAdapter.actualizarDatos(listaFavoritos, setIdsFavoritos)

            if (listaFavoritos.isEmpty()) {
                binding.layoutVacioFavoritos.visibility = View.VISIBLE
            }
        }
    }
}
