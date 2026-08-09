package com.example.gestiondecompras;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.gestiondecompras.utils.CalculadoraGanancias;

import org.junit.Test;

public class CalculadoraGananciasTest {

    @Test
    public void calcularTotal_sumaCompraMasGanancia() {
        assertEquals(125.0, CalculadoraGanancias.calcularTotal(100.0, 25.0), 0.001);
    }

    @Test
    public void calcularGananciaDesdePorcentaje_aplicaPorcentaje() {
        assertEquals(20.0, CalculadoraGanancias.calcularGananciaDesdePorcentaje(200.0, 10.0), 0.001);
        assertEquals(0.0, CalculadoraGanancias.calcularGananciaDesdePorcentaje(0.0, 10.0), 0.001);
        assertEquals(0.0, CalculadoraGanancias.calcularGananciaDesdePorcentaje(-50.0, 10.0), 0.001);
    }

    @Test
    public void calcularPorcentajeGanancia_divideGananciaEntreCompra() {
        assertEquals(25.0, CalculadoraGanancias.calcularPorcentajeGanancia(200.0, 50.0), 0.001);
        assertEquals(0.0, CalculadoraGanancias.calcularPorcentajeGanancia(0.0, 50.0), 0.001);
    }

    @Test
    public void calcularGananciaNeta_restaCostos() {
        assertEquals(70.0, CalculadoraGanancias.calcularGananciaNeta(100.0, 30.0), 0.001);
    }

    @Test
    public void calcularPrecioVenta_aplicaMargen() {
        assertEquals(125.0, CalculadoraGanancias.calcularPrecioVenta(100.0, 25.0), 0.001);
        assertEquals(0.0, CalculadoraGanancias.calcularPrecioVenta(0.0, 25.0), 0.001);
    }

    @Test
    public void esMontoValido_rechazaNegativosEInvalidos() {
        assertTrue(CalculadoraGanancias.esMontoValido(0.0));
        assertTrue(CalculadoraGanancias.esMontoValido(150.75));
        assertFalse(CalculadoraGanancias.esMontoValido(-1.0));
        assertFalse(CalculadoraGanancias.esMontoValido(Double.NaN));
        assertFalse(CalculadoraGanancias.esMontoValido(Double.POSITIVE_INFINITY));
    }

    @Test
    public void redondear_dosDecimales() {
        assertEquals(1.23, CalculadoraGanancias.redondear(1.234), 0.001);
        assertEquals(1.24, CalculadoraGanancias.redondear(1.236), 0.001);
        assertEquals(0.0, CalculadoraGanancias.redondear(Double.NaN), 0.001);
    }

    @Test
    public void formatearMonto_formatoRD() {
        String esperado = String.format("RD$ %,.2f", 1234.5);
        assertEquals(esperado, CalculadoraGanancias.formatearMonto(1234.5));
        assertEquals("RD$ 0.00", CalculadoraGanancias.formatearMonto(Double.NaN));
        assertEquals("RD$ 0.00", CalculadoraGanancias.formatearMonto(-5.0));
    }

    @Test
    public void formatearPorcentaje_unDecimal() {
        String esperado = String.format("%.1f%%", 25.5);
        assertEquals(esperado, CalculadoraGanancias.formatearPorcentaje(25.5));
        assertEquals("0%", CalculadoraGanancias.formatearPorcentaje(Double.NaN));
    }

    @Test
    public void sumarMontos_ignoraInvalidos() {
        assertEquals(35.5, CalculadoraGanancias.sumarMontos(10.0, 25.5, -5.0), 0.001);
        assertEquals(0.0, CalculadoraGanancias.sumarMontos(null), 0.001);
    }

    @Test
    public void calcularPromedioGanancia_devuelveMedia() {
        assertEquals(25.0, CalculadoraGanancias.calcularPromedioGanancia(100.0, 4), 0.001);
        assertEquals(0.0, CalculadoraGanancias.calcularPromedioGanancia(100.0, 0), 0.001);
    }
}