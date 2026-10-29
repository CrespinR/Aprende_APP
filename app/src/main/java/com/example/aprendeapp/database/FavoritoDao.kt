package com.example.aprendeapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoritoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorito(favorito: FavoritoEntity)

    @Delete
    suspend fun deleteFavorito(favorito: FavoritoEntity)

    @Query("DELETE FROM favoritos WHERE usuarioId = :usuarioId AND recursoId = :recursoId")
    suspend fun removeFavorito(usuarioId: String, recursoId: String)

    @Query("SELECT * FROM favoritos WHERE usuarioId = :usuarioId ORDER BY fechaAgregado DESC")
    suspend fun getFavoritosByUsuario(usuarioId: String): List<FavoritoEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM favoritos WHERE usuarioId = :usuarioId AND recursoId = :recursoId)")
    suspend fun isFavorito(usuarioId: String, recursoId: String): Boolean
}
