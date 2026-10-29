package com.example.aprendeapp.util

object Constants {
    const val BASE_URL = "https://6ae0095b8e42963c8d5ec19c.mockapi.io/"
    const val ENDPOINT_RECURSOS = "recursos"

    const val TIPO_TODOS = "Todos"
    const val TIPO_LIBRO = "Libro"
    const val TIPO_VIDEO = "Video"
    const val TIPO_ARTICULO = "Articulo"
    const val TIPO_TUTORIAL = "Tutorial"

    val TIPOS_PERMITIDOS = listOf(TIPO_LIBRO, TIPO_VIDEO, TIPO_ARTICULO, TIPO_TUTORIAL)

    val DOCENTE_EMAILS = setOf(
        "docente@udb.edu.sv",
        "docente@aprendeapp.com",
        "admin@udb.edu.sv",
        "profesor@udb.edu.sv"
    )

    const val EXTRA_RECURSO_ID = "extra_recurso_id"
    const val EXTRA_RECURSO_EDIT = "extra_recurso_edit"
}
