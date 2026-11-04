package com.upchiapas.plantas.plants.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class PlantTest {

    private val today = LocalDate.of(2026, 11, 3)

    private fun plant(lastWatered: LocalDate, interval: Int = 3) = Plant(
        id = 1,
        name = "Monstera",
        species = "Monstera deliciosa",
        wateringIntervalDays = interval,
        plantingDate = LocalDate.of(2026, 1, 10),
        lastWateredDate = lastWatered,
        needsSun = false
    )

    @Test
    fun nextWateringDate_sumaElIntervaloAlUltimoRiego() {
        val p = plant(lastWatered = LocalDate.of(2026, 11, 1), interval = 3)
        assertEquals(LocalDate.of(2026, 11, 4), p.nextWateringDate())
    }

    @Test
    fun daysUntilNextWatering_esPositivoSiFaltanDias() {
        val p = plant(lastWatered = LocalDate.of(2026, 11, 2), interval = 3)
        assertEquals(2L, p.daysUntilNextWatering(today))
        assertFalse(p.needsWateringToday(today))
    }

    @Test
    fun needsWateringToday_esTrueElDiaExacto() {
        val p = plant(lastWatered = LocalDate.of(2026, 10, 31), interval = 3)
        assertEquals(0L, p.daysUntilNextWatering(today))
        assertTrue(p.needsWateringToday(today))
    }

    @Test
    fun needsWateringToday_esTrueSiYaSePaso() {
        val p = plant(lastWatered = LocalDate.of(2026, 10, 25), interval = 3)
        assertTrue(p.daysUntilNextWatering(today) < 0)
        assertTrue(p.needsWateringToday(today))
    }
}
