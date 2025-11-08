package com.mob.proyectoandroid.data.network


import com.mob.proyectoandroid.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface SupabaseApiService {

    // Auth endpoints
    @POST("auth/v1/signup")
    suspend fun signUp(@Body request: AuthRequest): Response<AuthResponse>

    @POST("auth/v1/token?grant_type=password")
    suspend fun signIn(@Body request: AuthRequest): Response<AuthResponse>

    @POST("auth/v1/logout")
    suspend fun signOut(@Header("Authorization") token: String): Response<Unit>

    // Task endpoints
    @GET("rest/v1/tasks")
    suspend fun getTasks(
        @Header("Authorization") token: String,
        @Query("user_id") userId: String,
        @Query("select") select: String = "*"
    ): Response<List<Task>>

    @POST("rest/v1/tasks")
    suspend fun createTask(
        @Header("Authorization") token: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Body task: TaskRequest
    ): Response<List<Task>>

    @PATCH("rest/v1/tasks")
    suspend fun updateTask(
        @Header("Authorization") token: String,
        @Header("Prefer") prefer: String = "return=representation",
        @Query("id") taskId: String,
        @Body taskUpdate: TaskUpdateRequest
    ): Response<List<Task>>

    @DELETE("rest/v1/tasks")
    suspend fun deleteTask(
        @Header("Authorization") token: String,
        @Query("id") taskId: String
    ): Response<Unit>
}
