package com.example.aprendeapp.controller

import com.example.aprendeapp.model.Usuario
import com.example.aprendeapp.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class AuthController private constructor() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        @Volatile
        private var instance: AuthController? = null

        fun getInstance(): AuthController {
            return instance ?: synchronized(this) {
                instance ?: AuthController().also { instance = it }
            }
        }
    }

    fun obtenerUsuarioActual(): Usuario? {
        val firebaseUser = auth.currentUser ?: return null
        val email = firebaseUser.email ?: ""
        val nombre = firebaseUser.displayName.takeIf { !it.isNullOrBlank() } ?: email.substringBefore("@")
        val esDocente = determinarEsDocente(email)
        return Usuario(
            uid = firebaseUser.uid,
            nombre = nombre,
            email = email,
            esDocente = esDocente
        )
    }

    fun estaSesionIniciada(): Boolean {
        return auth.currentUser != null
    }

    fun registrarUsuario(
        nombre: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(nombre)
                            .build()
                        firebaseUser.updateProfile(profileUpdates)
                            .addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    onResult(true, null)
                                } else {
                                    onResult(true, "Usuario creado, pero no se pudo actualizar el nombre.")
                                }
                            }
                    } else {
                        onResult(true, null)
                    }
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Error al registrar usuario.")
                }
            }
    }

    fun iniciarSesion(
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "Credenciales inválidas.")
                }
            }
    }

    fun recuperarContrasena(
        email: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Se ha enviado un correo para restablecer la contraseña.")
                } else {
                    onResult(false, task.exception?.localizedMessage ?: "No se pudo enviar el correo de recuperación.")
                }
            }
    }

    fun cerrarSesion() {
        auth.signOut()
    }

    private fun determinarEsDocente(email: String): Boolean {
        if (email.isBlank()) return false
        val cleanEmail = email.trim().lowercase()
        return Constants.DOCENTE_EMAILS.any { cleanEmail.equals(it, ignoreCase = true) }
    }
}
