package com.example.aprendeapp.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.aprendeapp.R
import com.example.aprendeapp.controller.AuthController
import com.example.aprendeapp.databinding.ActivityPerfilBinding

class PerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private val authController = AuthController.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegresarPerfil.setOnClickListener {
            finish()
        }

        binding.btnPerfilCerrarSesion.setOnClickListener {
            authController.cerrarSesion()
            irALogin()
        }

        cargarInformacionPerfil()
    }

    private fun cargarInformacionPerfil() {
        val usuario = authController.obtenerUsuarioActual()
        if (usuario == null) {
            irALogin()
            return
        }

        binding.tvPerfilNombre.text = usuario.nombre
        binding.tvPerfilEmail.text = usuario.email
        binding.tvPerfilUid.text = usuario.uid

        if (usuario.esDocente) {
            binding.tvPerfilRol.text = getString(R.string.rol_docente)
        } else {
            binding.tvPerfilRol.text = getString(R.string.rol_estudiante)
        }
    }

    private fun irALogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
