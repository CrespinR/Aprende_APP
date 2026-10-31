package com.example.aprendeapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FavoritoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFavorito(favorito: FavoritoEntity): Long

    @Delete
    fun deleteFavorito(favorito: FavoritoEntity): Int

    @Query("DELETE FROM favoritos WHERE usuarioId = :usuarioId AND recursoId = :recursoId")
    fun removeFavorito(usuarioId: String, recursoId: String): Int

    @Query("SELECT * FROM favoritos WHERE usuarioId = :usuarioId ORDER BY fechaAgregado DESC")
    fun getFavoritosByUsuario(usuarioId: String): List<FavoritoEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM favoritos WHERE usuarioId = :usuarioId AND recursoId = :recursoId)")
    fun isFavorito(usuarioId: String, recursoId: String): Boolean
}
