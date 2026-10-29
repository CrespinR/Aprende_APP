package com.example.aprendeapp.view

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aprendeapp.R
import com.example.aprendeapp.controller.AuthController
import com.example.aprendeapp.databinding.ActivityRegistroBinding
import com.example.aprendeapp.util.PasswordValidator
import com.google.android.material.snackbar.Snackbar

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding
    private val authController = AuthController.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnRegistrarse.setOnClickListener {
            realizarRegistro()
        }

        binding.tvIniciarSesion.setOnClickListener {
            finish()
        }
    }

    private fun realizarRegistro() {
        val nombre = binding.etNombre.text?.toString()?.trim() ?: ""
        val email = binding.etEmail.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString()?.trim() ?: ""
        val confirmPassword = binding.etConfirmPassword.text?.toString()?.trim() ?: ""

        // Limpiar errores previos
        binding.tilNombre.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        var hayError = false

        if (nombre.isBlank()) {
            binding.tilNombre.error = getString(R.string.err_nombre_vacio)
            hayError = true
        }

        if (email.isBlank()) {
            binding.tilEmail.error = getString(R.string.err_correo_vacio)
            hayError = true
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = getString(R.string.err_correo_invalido)
            hayError = true
        }

        val validationResult = PasswordValidator.validate(password)
        if (validationResult is PasswordValidator.ValidationResult.Invalid) {
            binding.tilPassword.error = validationResult.reason
            hayError = true
        }

        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.err_contrasenas_no_coinciden)
            hayError = true
        }

        if (hayError) return

        mostrarCargando(true)

        authController.registrarUsuario(nombre, email, password) { exito, errorMsg ->
            mostrarCargando(false)
            if (exito) {
                Toast.makeText(this, getString(R.string.msg_registro_exitoso), Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Snackbar.make(binding.root, errorMsg ?: getString(R.string.error_desconocido), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun mostrarCargando(cargando: Boolean) {
        binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        binding.btnRegistrarse.isEnabled = !cargando
    }
}
