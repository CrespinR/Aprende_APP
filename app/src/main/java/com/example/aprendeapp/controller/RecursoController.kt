package com.example.aprendeapp.controller

import com.example.aprendeapp.model.Recurso
import com.example.aprendeapp.network.RecursoApiService
import com.example.aprendeapp.network.RetrofitClient
import com.example.aprendeapp.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecursoController(
    private val apiService: RecursoApiService = RetrofitClient.apiService
) {

    suspend fun obtenerRecursos(): Result<List<Recurso>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRecursos()
            if (response.isSuccessful) {
                val body = response.body()
                Result.success(body ?: emptyList())
            } else {
                Result.failure(Exception("Error HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.localizedMessage ?: "Compruebe su conexión a Internet"}"))
        }
    }

    suspend fun obtenerRecursoPorId(id: String): Result<Recurso> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRecursoById(id)
            if (response.isSuccessful) {
                val recurso = response.body()
                if (recurso != null) {
                    Result.success(recurso)
                } else {
                    Result.failure(Exception("Recurso no encontrado"))
                }
            } else {
                Result.failure(Exception("Error HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.localizedMessage}"))
        }
    }

    suspend fun crearRecurso(recurso: Recurso): Result<Recurso> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createRecurso(recurso)
            if (response.isSuccessful) {
                val creado = response.body()
                if (creado != null) {
                    Result.success(creado)
                } else {
                    Result.failure(Exception("Error al crear el recurso en la API"))
                }
            } else {
                Result.failure(Exception("Error HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error al conectar con la API: ${e.localizedMessage}"))
        }
    }

    suspend fun actualizarRecurso(id: String, recurso: Recurso): Result<Recurso> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateRecurso(id, recurso)
            if (response.isSuccessful) {
                val actualizado = response.body()
                if (actualizado != null) {
                    Result.success(actualizado)
                } else {
                    Result.failure(Exception("Respuesta vacía al actualizar"))
                }
            } else {
                Result.failure(Exception("Error HTTP ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de conexión: ${e.localizedMessage}"))
        }
    }

    suspend fun eliminarRecurso(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteRecurso(id)
            if (response.isSuccessful) {
                Result.success(true)
            } else {
                Result.failure(Exception("Error al eliminar (HTTP ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error de red: ${e.localizedMessage}"))
        }
    }

    fun buscarYFiltrar(
        lista: List<Recurso>,
        consulta: String,
        tipoFiltro: String,
        ordenAscendente: Boolean = true
    ): List<Recurso> {
        var resultado = lista

        // 1. Filtrar por tipo
        if (tipoFiltro != Constants.TIPO_TODOS) {
            resultado = resultado.filter {
                it.tipo.equals(tipoFiltro, ignoreCase = true)
            }
        }

        // 2. Buscar por ID, Título o Tipo
        if (consulta.isNotBlank()) {
            val queryClean = consulta.trim().lowercase()
            resultado = resultado.filter { rec ->
                (rec.id?.lowercase()?.contains(queryClean) == true) ||
                        (rec.titulo.lowercase().contains(queryClean)) ||
                        (rec.tipo.lowercase().contains(queryClean)) ||
                        (rec.descripcion.lowercase().contains(queryClean))
            }
        }

        // 3. Ordenar por título
        resultado = if (ordenAscendente) {
            resultado.sortedBy { it.titulo.lowercase() }
        } else {
            resultado.sortedByDescending { it.titulo.lowercase() }
        }

        return resultado
    }
}
