package com.upchiapas.plantas.plants.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Cuerpo para crear o actualizar una planta (sin id: lo asigna el servidor). */
@Serializable
data class PlantRequestDto(
    @SerialName("name") val name: String,
    @SerialName("species") val species: String,
    @SerialName("wateringIntervalDays") val wateringIntervalDays: Int,
    @SerialName("plantingDate") val plantingDate: String,
    @SerialName("lastWateredDate") val lastWateredDate: String,
    @SerialName("needsSun") val needsSun: Boolean
)
