package com.upchiapas.plantas.plants.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Modelo de dominio del recurso principal. No conoce DTOs ni detalles de red.
 */
data class Plant(
    val id: Int,
    val name: String,
    val species: String,
    val wateringIntervalDays: Int,
    val plantingDate: LocalDate,
    val lastWateredDate: LocalDate,
    val needsSun: Boolean
) {
    /** Fecha en que le toca el siguiente riego. */
    fun nextWateringDate(): LocalDate = lastWateredDate.plusDays(wateringIntervalDays.toLong())

    /** Días que faltan para el siguiente riego; negativo si ya se pasó. */
    fun daysUntilNextWatering(today: LocalDate = LocalDate.now()): Long =
        ChronoUnit.DAYS.between(today, nextWateringDate())

    /** True si hoy (o antes) le tocaba riego. */
    fun needsWateringToday(today: LocalDate = LocalDate.now()): Boolean =
        daysUntilNextWatering(today) <= 0
}
