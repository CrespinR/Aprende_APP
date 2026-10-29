package com.example.aprendeapp.controller

import android.content.Context
import com.example.aprendeapp.database.AppDatabase
import com.example.aprendeapp.database.FavoritoEntity
import com.example.aprendeapp.model.Recurso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FavoritoController {

    suspend fun toggleFavorito(
        context: Context,
        usuarioId: String,
        recursoId: String
    ): Boolean = withContext(Dispatchers.IO) {
        val dao = AppDatabase.getDatabase(context).favoritoDao()
        val esFav = dao.isFavorito(usuarioId, recursoId)
        if (esFav) {
            dao.removeFavorito(usuarioId, recursoId)
            false
        } else {
            dao.insertFavorito(FavoritoEntity(usuarioId = usuarioId, recursoId = recursoId))
            true
        }
    }

    suspend fun esFavorito(
        context: Context,
        usuarioId: String,
        recursoId: String
    ): Boolean = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).favoritoDao().isFavorito(usuarioId, recursoId)
    }

    suspend fun obtenerFavoritosUsuario(
        context: Context,
        usuarioId: String
    ): List<FavoritoEntity> = withContext(Dispatchers.IO) {
        AppDatabase.getDatabase(context).favoritoDao().getFavoritosByUsuario(usuarioId)
    }

    suspend fun filtrarRecursosFavoritos(
        context: Context,
        usuarioId: String,
        todosRecursos: List<Recurso>
    ): List<Recurso> = withContext(Dispatchers.IO) {
        val favoritos = AppDatabase.getDatabase(context).favoritoDao().getFavoritosByUsuario(usuarioId)
        val idsFavoritos = favoritos.map { it.recursoId }.toSet()
        todosRecursos.filter { rec -> rec.id != null && idsFavoritos.contains(rec.id) }
    }
}
