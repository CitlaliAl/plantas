package com.upchiapas.plantas.plants.data.repository

import com.upchiapas.plantas.core.network.safeApiCall
import com.upchiapas.plantas.plants.data.mapper.toDomain
import com.upchiapas.plantas.plants.data.mapper.toDomainList
import com.upchiapas.plantas.plants.data.mapper.toRequestDto
import com.upchiapas.plantas.plants.data.remote.PlantsApiService
import com.upchiapas.plantas.plants.domain.model.Plant
import com.upchiapas.plantas.plants.domain.repository.PlantsRepository
import kotlinx.serialization.json.Json

class PlantsRepositoryImpl(
    private val apiService: PlantsApiService,
    private val json: Json
) : PlantsRepository {

    override suspend fun getPlants(): Result<List<Plant>> =
        safeApiCall(json) { apiService.getPlants().toDomainList() }

    override suspend fun getPlant(id: Int): Result<Plant> =
        safeApiCall(json) { apiService.getPlant(id).toDomain() }

    override suspend fun createPlant(plant: Plant): Result<Plant> =
        safeApiCall(json) { apiService.createPlant(plant.toRequestDto()).toDomain() }

    override suspend fun updatePlant(plant: Plant): Result<Plant> =
        safeApiCall(json) { apiService.updatePlant(plant.id, plant.toRequestDto()).toDomain() }

    override suspend fun deletePlant(id: Int): Result<Unit> =
        safeApiCall(json) { apiService.deletePlant(id) }
}
