package com.example.aprendeapp.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.aprendeapp.R
import com.example.aprendeapp.controller.AuthController
import com.example.aprendeapp.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val authController = AuthController.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (authController.estaSesionIniciada()) {
            irAMainActivity()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            realizarLogin()
        }

        binding.tvRegistrarse.setOnClickListener {
            startActivity(Intent(this, RegistroActivity::class.java))
        }

        binding.tvOlvideContrasena.setOnClickListener {
            mostrarDialogoRecuperar()
        }
    }

    private fun realizarLogin() {
        val email = binding.etEmail.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString()?.trim() ?: ""

        if (email.isBlank()) {
            binding.tilEmail.error = getString(R.string.err_correo_vacio)
            return
        } else {
            binding.tilEmail.error = null
        }

        if (password.isBlank()) {
            binding.tilPassword.error = getString(R.string.err_contrasena_vacia)
            return
        } else {
            binding.tilPassword.error = null
        }

        mostrarCargando(true)

        authController.iniciarSesion(email, password) { exito, errorMsg ->
            mostrarCargando(false)
            if (exito) {
                Toast.makeText(this, getString(R.string.msg_bienvenido), Toast.LENGTH_SHORT).show()
                irAMainActivity()
            } else {
                Snackbar.make(binding.root, errorMsg ?: getString(R.string.error_desconocido), Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun mostrarDialogoRecuperar() {
        val inputEmail = EditText(this)
        inputEmail.hint = getString(R.string.correo_label)
        inputEmail.setPadding(40, 20, 40, 20)

        val currentEmail = binding.etEmail.text?.toString()?.trim() ?: ""
        if (currentEmail.isNotBlank()) {
            inputEmail.setText(currentEmail)
        }

        AlertDialog.Builder(this)
            .setTitle(R.string.titulo_recuperar)
            .setMessage(R.string.msg_recuperar)
            .setView(inputEmail)
            .setPositiveButton(R.string.btn_enviar_recuperacion) { dialog, _ ->
                val email = inputEmail.text.toString().trim()
                if (email.isNotBlank()) {
                    mostrarCargando(true)
                    authController.recuperarContrasena(email) { exito, mensaje ->
                        mostrarCargando(false)
                        Toast.makeText(this, mensaje ?: "", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.err_correo_vacio), Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancelar) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun mostrarCargando(cargando: Boolean) {
        binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !cargando
    }

    private fun irAMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
