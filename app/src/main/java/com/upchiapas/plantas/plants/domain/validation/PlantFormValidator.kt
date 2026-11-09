package com.upchiapas.plantas.plants.domain.validation

import java.time.LocalDate

/** Valores tal como los escribe la persona en el formulario (todo texto). */
data class PlantFormInput(
    val name: String,
    val species: String,
    val wateringIntervalDays: String,
    val plantingDate: String,
    val lastWateredDate: String
)

/**
 * Mismas reglas que el servidor. Las claves coinciden con los campos de la API
 * para poder mezclar errores locales y errores devueltos por el servidor.
 */
object PlantFormValidator {

    fun parseDate(text: String): LocalDate? = runCatching { LocalDate.parse(text.trim()) }.getOrNull()

    fun validate(input: PlantFormInput, today: LocalDate = LocalDate.now()): Map<String, String> = buildMap {
        val name = input.name.trim()
        val species = input.species.trim()
        if (name.length !in 2..50) put("name", "El nombre debe tener entre 2 y 50 caracteres")
        if (species.isEmpty() || species.length > 80) put("species", "La especie es obligatoria (máx. 80 caracteres)")

        val interval = input.wateringIntervalDays.trim().toIntOrNull()
        if (interval == null || interval !in 1..60) {
            put("wateringIntervalDays", "Los días entre riegos deben ser un entero entre 1 y 60")
        }

        val planting = parseDate(input.plantingDate)
        if (planting == null) put("plantingDate", "Usa el formato aaaa-mm-dd")
        else if (planting.isAfter(today)) put("plantingDate", "La fecha de siembra no puede ser futura")

        val watered = parseDate(input.lastWateredDate)
        if (watered == null) put("lastWateredDate", "Usa el formato aaaa-mm-dd")
        else if (watered.isAfter(today)) put("lastWateredDate", "El último riego no puede ser futuro")
        else if (planting != null && !containsKey("plantingDate") && watered.isBefore(planting)) {
            put("lastWateredDate", "El último riego no puede ser anterior a la siembra")
        }
    }
}
