package com.upchiapas.plantas.plants.data.remote

import com.upchiapas.plantas.plants.data.remote.dto.PlantDto
import com.upchiapas.plantas.plants.data.remote.dto.PlantRequestDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PlantsApiService {

    @GET("plants")
    suspend fun getPlants(): List<PlantDto>

    @GET("plants/{id}")
    suspend fun getPlant(@Path("id") id: Int): PlantDto

    @POST("plants")
    suspend fun createPlant(@Body plant: PlantRequestDto): PlantDto

    @PUT("plants/{id}")
    suspend fun updatePlant(@Path("id") id: Int, @Body plant: PlantRequestDto): PlantDto

    @DELETE("plants/{id}")
    suspend fun deletePlant(@Path("id") id: Int)
}
