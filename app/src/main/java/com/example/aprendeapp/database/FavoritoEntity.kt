package com.example.aprendeapp.database

import androidx.room.Entity

@Entity(tableName = "favoritos", primaryKeys = ["usuarioId", "recursoId"])
data class FavoritoEntity(
    val usuarioId: String,
    val recursoId: String,
    val fechaAgregado: Long = System.currentTimeMillis()
)
