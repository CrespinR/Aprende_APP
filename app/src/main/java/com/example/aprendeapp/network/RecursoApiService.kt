package com.example.aprendeapp.network

import com.example.aprendeapp.model.Recurso
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RecursoApiService {

    @GET("recursos")
    suspend fun getRecursos(): Response<List<Recurso>>

    @GET("recursos/{id}")
    suspend fun getRecursoById(@Path("id") id: String): Response<Recurso>

    @POST("recursos")
    suspend fun createRecurso(@Body recurso: Recurso): Response<Recurso>

    @PUT("recursos/{id}")
    suspend fun updateRecurso(@Path("id") id: String, @Body recurso: Recurso): Response<Recurso>

    @DELETE("recursos/{id}")
    suspend fun deleteRecurso(@Path("id") id: String): Response<Recurso>
}
