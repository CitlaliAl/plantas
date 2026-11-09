package com.upchiapas.plantas.plants.ui

import com.upchiapas.plantas.plants.domain.model.Plant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val displayFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun LocalDate.display(): String = format(displayFormat)

data class WateringStatus(val text: String, val isUrgent: Boolean)

/** Texto de estado del riego para la lista y el detalle. */
fun Plant.wateringStatus(today: LocalDate = LocalDate.now()): WateringStatus {
    val days = daysUntilNextWatering(today)
    return when {
        days < 0 -> WateringStatus("Atrasada ${-days} ${if (-days == 1L) "día" else "días"}", true)
        days == 0L -> WateringStatus("Toca regar hoy", true)
        days == 1L -> WateringStatus("Riego mañana", false)
        else -> WateringStatus("Riego en $days días", false)
    }
}
