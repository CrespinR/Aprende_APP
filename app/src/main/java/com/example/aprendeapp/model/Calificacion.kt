package com.example.aprendeapp.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Calificacion(
    @SerializedName("usuarioId")
    val usuarioId: String = "",
    @SerializedName("puntuacion")
    val puntuacion: Double = 0.0
) : Serializable
