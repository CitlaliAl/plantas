package com.upchiapas.plantas.plants.domain.repository

import com.upchiapas.plantas.plants.domain.model.Plant

/**
 * Contrato de acceso a datos de plantas. Expone solo modelos de dominio;
 * la implementación vive en la capa data.
 */
interface PlantsRepository {
    suspend fun getPlants(): Result<List<Plant>>
    suspend fun getPlant(id: Int): Result<Plant>
    suspend fun createPlant(plant: Plant): Result<Plant>
    suspend fun updatePlant(plant: Plant): Result<Plant>
    suspend fun deletePlant(id: Int): Result<Unit>
}
