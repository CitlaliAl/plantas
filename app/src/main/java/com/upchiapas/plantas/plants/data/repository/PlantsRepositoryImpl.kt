package com.upchiapas.plantas.plants.data.repository

import com.upchiapas.plantas.plants.data.mapper.toDomain
import com.upchiapas.plantas.plants.data.mapper.toDomainList
import com.upchiapas.plantas.plants.data.mapper.toRequestDto
import com.upchiapas.plantas.plants.data.remote.PlantsApiService
import com.upchiapas.plantas.plants.domain.model.Plant
import com.upchiapas.plantas.plants.domain.repository.PlantsRepository
import retrofit2.HttpException
import java.io.IOException

class PlantsRepositoryImpl(
    private val apiService: PlantsApiService
) : PlantsRepository {

    override suspend fun getPlants(): Result<List<Plant>> =
        call { apiService.getPlants().toDomainList() }

    override suspend fun getPlant(id: Int): Result<Plant> =
        call { apiService.getPlant(id).toDomain() }

    override suspend fun createPlant(plant: Plant): Result<Plant> =
        call { apiService.createPlant(plant.toRequestDto()).toDomain() }

    override suspend fun updatePlant(plant: Plant): Result<Plant> =
        call { apiService.updatePlant(plant.id, plant.toRequestDto()).toDomain() }

    override suspend fun deletePlant(id: Int): Result<Unit> =
        call { apiService.deletePlant(id) }

    private inline fun <T> call(block: () -> T): Result<T> =
        runCatching(block).fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(mapThrowable(it)) }
        )

    private fun mapThrowable(throwable: Throwable): Throwable = when (throwable) {
        is HttpException -> Exception("Error HTTP ${throwable.code()}: ${throwable.message()}")
        is IOException -> Exception("Error de red: comprueba tu conexión")
        else -> throwable
    }
}
