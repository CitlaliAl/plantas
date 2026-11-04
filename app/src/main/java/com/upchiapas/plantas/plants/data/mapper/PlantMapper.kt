package com.upchiapas.plantas.plants.data.mapper

import com.upchiapas.plantas.plants.data.remote.dto.PlantDto
import com.upchiapas.plantas.plants.data.remote.dto.PlantRequestDto
import com.upchiapas.plantas.plants.domain.model.Plant
import java.time.LocalDate

fun PlantDto.toDomain(): Plant {
    val planting = plantingDate?.let(LocalDate::parse) ?: LocalDate.now()
    return Plant(
        id = id ?: 0,
        name = name.orEmpty(),
        species = species.orEmpty(),
        wateringIntervalDays = wateringIntervalDays ?: 1,
        plantingDate = planting,
        lastWateredDate = lastWateredDate?.let(LocalDate::parse) ?: planting,
        needsSun = needsSun ?: false
    )
}

fun List<PlantDto>.toDomainList(): List<Plant> = map { it.toDomain() }

fun Plant.toRequestDto(): PlantRequestDto = PlantRequestDto(
    name = name,
    species = species,
    wateringIntervalDays = wateringIntervalDays,
    plantingDate = plantingDate.toString(),
    lastWateredDate = lastWateredDate.toString(),
    needsSun = needsSun
)
