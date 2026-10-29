package com.example.aprendeapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Recurso(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("titulo")
    val titulo: String = "",

    @SerializedName("descripcion")
    val descripcion: String = "",

    @SerializedName("tipo")
    val tipo: String = "",

    @SerializedName("enlace")
    val enlace: String = "",

    @SerializedName("imagen")
    val imagen: String = "",

    @SerializedName("fechaCreacion")
    val fechaCreacion: String = "",

    @SerializedName("calificaciones")
    val calificaciones: List<Calificacion>? = null
) : Serializable {

    fun getPromedioCalificaciones(): Double {
        val list = calificaciones
        if (list.isNullOrEmpty()) return 0.0
        val sum = list.sumOf { it.puntuacion }
        val avg = sum / list.size
        return Math.round(avg * 10.0) / 10.0
    }

    fun getCalificacionUsuario(usuarioId: String): Double {
        return calificaciones?.firstOrNull { it.usuarioId == usuarioId }?.puntuacion ?: 0.0
    }

    fun getTotalCalificaciones(): Int {
        return calificaciones?.size ?: 0
    }
}
