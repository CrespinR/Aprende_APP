package com.example.aprendeapp.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.example.aprendeapp.databinding.ActivityMainBinding
import com.example.aprendeapp.model.Recurso
import com.example.aprendeapp.util.Constants
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authController = AuthController.getInstance()
    private val recursoController = RecursoController()
    private val favoritoController = FavoritoController()

    private lateinit var recursoAdapter: RecursoAdapter
    private var listaOriginal: List<Recurso> = emptyList()
    private var setFavoritosIds: Set<String> = emptySet()

    private var filtroTipoActual: String = Constants.TIPO_TODOS
    private var ordenAscendente: Boolean = true
    private var consultaBusqueda: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val usuario = authController.obtenerUsuarioActual()
        if (usuario == null) {
            irALogin()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarHeaderUsuario()
        configurarRecyclerView()
        configurarFiltrosYBusqueda()
        configurarListeners()

        cargarDatos()
    }

    override fun onResume() {
        super.onResume()
        cargarFavoritosYActualizar()
    }

    private fun configurarHeaderUsuario() {
        val usuario = authController.obtenerUsuarioActual() ?: return
        binding.tvSaludo.text = getString(R.string.saludo_hola, usuario.nombre)

        if (usuario.esDocente) {
            binding.tvRolBadge.text = getString(R.string.rol_docente)
            binding.btnAdminPanel.visibility = View.VISIBLE
        } else {
            binding.tvRolBadge.text = getString(R.string.rol_estudiante)
            binding.btnAdminPanel.visibility = View.GONE
        }
    }

    private fun configurarRecyclerView() {
        val usuario = authController.obtenerUsuarioActual()
        recursoAdapter = RecursoAdapter(
            context = this,
            listaRecursos = emptyList(),
            idsFavoritos = setFavoritosIds,
            esModoAdmin = false,
            onDetallesClick = { recurso ->
                val intent = Intent(this, DetalleRecursoActivity::class.java)
                intent.putExtra(Constants.EXTRA_RECURSO_ID, recurso.id)
                startActivity(intent)
            },
            onFavoritoToggle = { recurso ->
                recurso.id?.let { recursoId ->
                    usuario?.uid?.let { uid ->
                        toggleFavorito(uid, recursoId)
                    }
                }
            }
        )

        binding.rvRecursos.layoutManager = LinearLayoutManager(this)
        binding.rvRecursos.adapter = recursoAdapter
    }

    private fun configurarFiltrosYBusqueda() {
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                consultaBusqueda = s?.toString() ?: ""
                aplicarFiltros()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.chipGroupFiltros.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            val chipId = checkedIds.first()
            val chip = findViewById<Chip>(chipId)
            val tipoText = chip?.text?.toString() ?: Constants.TIPO_TODOS

            filtroTipoActual = when {
                tipoText.equals(getString(R.string.filtro_libro), ignoreCase = true) -> Constants.TIPO_LIBRO
                tipoText.equals(getString(R.string.filtro_video), ignoreCase = true) -> Constants.TIPO_VIDEO
                tipoText.equals(getString(R.string.filtro_articulo), ignoreCase = true) -> Constants.TIPO_ARTICULO
                tipoText.equals(getString(R.string.filtro_tutorial), ignoreCase = true) -> Constants.TIPO_TUTORIAL
                else -> Constants.TIPO_TODOS
            }
            aplicarFiltros()
        }

        binding.btnOrdenar.setOnClickListener {
            ordenAscendente = !ordenAscendente
            val toastMsg = if (ordenAscendente) getString(R.string.orden_asc) else getString(R.string.orden_desc)
            Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show()
            aplicarFiltros()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            cargarDatos()
        }
    }

    private fun configurarListeners() {
        binding.btnAdminPanel.setOnClickListener {
            startActivity(Intent(this, AdminRecursosActivity::class.java))
        }

        binding.btnFavoritos.setOnClickListener {
            startActivity(Intent(this, FavoritosActivity::class.java))
        }

        binding.btnPerfil.setOnClickListener {
            startActivity(Intent(this, PerfilActivity::class.java))
        }

        binding.btnCerrarSesion.setOnClickListener {
            authController.cerrarSesion()
            irALogin()
        }

        binding.btnReintentar.setOnClickListener {
            cargarDatos()
        }
    }

    private fun cargarDatos() {
        mostrarCargando(true)
        ocultarEstados()

        lifecycleScope.launch {
            val usuario = authController.obtenerUsuarioActual()
            if (usuario != null) {
                val favoritos = favoritoController.obtenerFavoritosUsuario(this@MainActivity, usuario.uid)
                setFavoritosIds = favoritos.map { it.recursoId }.toSet()
            }

            val result = recursoController.obtenerRecursos()
            mostrarCargando(false)
            binding.swipeRefreshLayout.isRefreshing = false

            result.onSuccess { recursos ->
                listaOriginal = recursos
                aplicarFiltros()
            }.onFailure { exception ->
                binding.layoutEstadoError.visibility = View.VISIBLE
                binding.tvMensajeError.text = exception.localizedMessage ?: getString(R.string.error_cargar_recursos)
            }
        }
    }

    private fun cargarFavoritosYActualizar() {
        lifecycleScope.launch {
            val usuario = authController.obtenerUsuarioActual() ?: return@launch
            val favoritos = favoritoController.obtenerFavoritosUsuario(this@MainActivity, usuario.uid)
            setFavoritosIds = favoritos.map { it.recursoId }.toSet()
            recursoAdapter.actualizarFavoritos(setFavoritosIds)
        }
    }

    private fun toggleFavorito(usuarioId: String, recursoId: String) {
        lifecycleScope.launch {
            val esAhoraFav = favoritoController.toggleFavorito(this@MainActivity, usuarioId, recursoId)
            val msg = if (esAhoraFav) getString(R.string.favorito_agregado) else getString(R.string.favorito_eliminado)
            Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()

            val favs = favoritoController.obtenerFavoritosUsuario(this@MainActivity, usuarioId)
            setFavoritosIds = favs.map { it.recursoId }.toSet()
            recursoAdapter.actualizarFavoritos(setFavoritosIds)
        }
    }

    private fun aplicarFiltros() {
        val filtrados = recursoController.buscarYFiltrar(
            lista = listaOriginal,
            consulta = consultaBusqueda,
            tipoFiltro = filtroTipoActual,
            ordenAscendente = ordenAscendente
        )

        recursoAdapter.actualizarDatos(filtrados, setFavoritosIds)

        if (filtrados.isEmpty() && listaOriginal.isNotEmpty()) {
            binding.layoutEstadoVacio.visibility = View.VISIBLE
        } else if (listaOriginal.isEmpty()) {
            binding.layoutEstadoVacio.visibility = View.VISIBLE
        } else {
            binding.layoutEstadoVacio.visibility = View.GONE
        }
    }

    private fun mostrarCargando(cargando: Boolean) {
        binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
    }

    private fun ocultarEstados() {
        binding.layoutEstadoVacio.visibility = View.GONE
        binding.layoutEstadoError.visibility = View.GONE
    }

    private fun irALogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
