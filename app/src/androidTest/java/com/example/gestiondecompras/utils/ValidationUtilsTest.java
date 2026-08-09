package com.example.gestiondecompras.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ValidationUtilsTest {

    @Test
    public void validateCliente_aceptaDatosValidos() {
        assertNull(ValidationUtils.validateCliente("Ana", "", ""));
        assertNull(ValidationUtils.validateCliente("Ana Lopez", "8095551234", "ana@test.com"));
    }

    @Test
    public void validateCliente_rechazaNombreInvalido() {
        assertEquals("El nombre es obligatorio", ValidationUtils.validateCliente("", "", ""));
        assertEquals("El nombre es obligatorio", ValidationUtils.validateCliente(null, "", ""));
        assertEquals("El nombre debe tener al menos 2 caracteres", ValidationUtils.validateCliente("A", "", ""));
    }

    @Test
    public void validateCliente_rechazaTelefonoYEmailInvalidos() {
        assertEquals("El teléfono no es válido", ValidationUtils.validateCliente("Ana", "123", ""));
        assertEquals("El email no es válido", ValidationUtils.validateCliente("Ana", "", "correo-roto"));
    }

    @Test
    public void validatePedido_validaClienteTiendaYMontos() {
        assertNull(ValidationUtils.validatePedido(100.0, 25.0, "Ana", "Temu"));

        assertEquals("Selecciona un cliente", ValidationUtils.validatePedido(100.0, 25.0, "", "Temu"));
        assertEquals("Selecciona un cliente", ValidationUtils.validatePedido(100.0, 25.0, null, "Temu"));
        assertEquals("Selecciona una tienda", ValidationUtils.validatePedido(100.0, 25.0, "Ana", ""));
        assertEquals("El monto de compra debe ser mayor a 0", ValidationUtils.validatePedido(0.0, 25.0, "Ana", "Temu"));
        assertEquals("El monto de compra debe ser mayor a 0", ValidationUtils.validatePedido(-5.0, 25.0, "Ana", "Temu"));
        assertEquals("La ganancia no puede ser negativa", ValidationUtils.validatePedido(100.0, -1.0, "Ana", "Temu"));
        assertEquals("Los montos no son válidos", ValidationUtils.validatePedido(Double.NaN, 25.0, "Ana", "Temu"));
        assertEquals("Los montos no son válidos", ValidationUtils.validatePedido(100.0, Double.POSITIVE_INFINITY, "Ana", "Temu"));
    }

    @Test
    public void isValidAmount_aceptaCeroYPositivos() {
        assertTrue(ValidationUtils.isValidAmount(0.0));
        assertTrue(ValidationUtils.isValidAmount(100.5));
        assertFalse(ValidationUtils.isValidAmount(-1.0));
        assertFalse(ValidationUtils.isValidAmount(Double.NaN));
    }

    @Test
    public void isValidPercentage_limiteMil() {
        assertTrue(ValidationUtils.isValidPercentage(0.0));
        assertTrue(ValidationUtils.isValidPercentage(100.0));
        assertTrue(ValidationUtils.isValidPercentage(1000.0));
        assertFalse(ValidationUtils.isValidPercentage(1000.01));
        assertFalse(ValidationUtils.isValidPercentage(-1.0));
        assertFalse(ValidationUtils.isValidPercentage(Double.NaN));
        assertFalse(ValidationUtils.isValidPercentage(Double.POSITIVE_INFINITY));
    }
}