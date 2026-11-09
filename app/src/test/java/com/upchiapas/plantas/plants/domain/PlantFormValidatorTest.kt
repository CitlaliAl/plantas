package com.upchiapas.plantas.plants.domain

import com.upchiapas.plantas.plants.domain.validation.PlantFormInput
import com.upchiapas.plantas.plants.domain.validation.PlantFormValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PlantFormValidatorTest {

    private val today = LocalDate.of(2026, 11, 8)
    private val valid = PlantFormInput("Helecho", "Nephrolepis exaltata", "3", "2026-05-20", "2026-11-01")

    @Test
    fun datos_validos_no_tienen_errores() {
        assertTrue(PlantFormValidator.validate(valid, today).isEmpty())
    }

    @Test
    fun intervalo_fuera_de_rango_o_no_numerico() {
        assertEquals(setOf("wateringIntervalDays"), PlantFormValidator.validate(valid.copy(wateringIntervalDays = "0"), today).keys)
        assertEquals(setOf("wateringIntervalDays"), PlantFormValidator.validate(valid.copy(wateringIntervalDays = "61"), today).keys)
        assertEquals(setOf("wateringIntervalDays"), PlantFormValidator.validate(valid.copy(wateringIntervalDays = ""), today).keys)
    }

    @Test
    fun fechas_futuras_o_con_formato_incorrecto() {
        assertEquals(setOf("plantingDate"), PlantFormValidator.validate(valid.copy(plantingDate = "2999-01-01"), today).keys)
        assertEquals(setOf("lastWateredDate"), PlantFormValidator.validate(valid.copy(lastWateredDate = "01/11/2026"), today).keys)
    }

    @Test
    fun ultimo_riego_no_puede_ser_anterior_a_la_siembra() {
        val errors = PlantFormValidator.validate(valid.copy(lastWateredDate = "2026-01-01"), today)
        assertEquals(setOf("lastWateredDate"), errors.keys)
    }

    @Test
    fun nombre_y_especie_obligatorios() {
        val errors = PlantFormValidator.validate(valid.copy(name = "A", species = " "), today)
        assertEquals(setOf("name", "species"), errors.keys)
    }
}
