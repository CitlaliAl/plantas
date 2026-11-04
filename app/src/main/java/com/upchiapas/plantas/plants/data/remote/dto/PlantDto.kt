package com.upchiapas.plantas.plants.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Planta tal como la devuelve la API. Las fechas viajan como texto "yyyy-MM-dd". */
@Serializable
data class PlantDto(
    @SerialName("id") val id: Int? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("species") val species: String? = null,
    @SerialName("wateringIntervalDays") val wateringIntervalDays: Int? = null,
    @SerialName("plantingDate") val plantingDate: String? = null,
    @SerialName("lastWateredDate") val lastWateredDate: String? = null,
    @SerialName("needsSun") val needsSun: Boolean? = null
)
