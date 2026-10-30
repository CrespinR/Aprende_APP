package com.example.aprendeapp.controller

import com.example.aprendeapp.model.Calificacion
import com.example.aprendeapp.model.Recurso

class CalificacionController(
    private val recursoController: RecursoController = RecursoController()
) {

    suspend fun calificarRecurso(
        recurso: Recurso,
        usuarioId: String,
        puntuacion: Double
    ): Result<Recurso> {
        val recursoId = recurso.id ?: return Result.failure(Exception("ID de recurso no válido"))

        val calificacionesActuales = recurso.calificaciones?.toMutableList() ?: mutableListOf()

        val index = calificacionesActuales.indexOfFirst { it.usuarioId == usuarioId }
        val nuevaCalificacion = Calificacion(usuarioId = usuarioId, puntuacion = puntuacion)

        if (index >= 0) {
            calificacionesActuales[index] = nuevaCalificacion
        } else {
            calificacionesActuales.add(nuevaCalificacion)
        }

        val recursoActualizado = recurso.copy(calificaciones = calificacionesActuales)

        return recursoController.actualizarRecurso(recursoId, recursoActualizado)
    }
}
