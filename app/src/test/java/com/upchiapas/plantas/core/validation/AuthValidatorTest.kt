package com.upchiapas.plantas.core.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun login_vacio_marca_correo_y_contrasena() {
        val errors = AuthValidator.validateLogin("", "")
        assertEquals(setOf("email", "password"), errors.keys)
    }

    @Test
    fun login_correcto_no_tiene_errores() {
        assertTrue(AuthValidator.validateLogin("demo@plantas.com", "plantas123").isEmpty())
    }

    @Test
    fun registro_valida_cada_campo() {
        val errors = AuthValidator.validateRegister(" ", "no-es-correo", "1234567", "otra")
        assertEquals(setOf("name", "email", "password", "confirmPassword"), errors.keys)
    }

    @Test
    fun registro_correcto_no_tiene_errores() {
        assertTrue(AuthValidator.validateRegister("Citlali", "c@mail.com", "12345678", "12345678").isEmpty())
    }
}
