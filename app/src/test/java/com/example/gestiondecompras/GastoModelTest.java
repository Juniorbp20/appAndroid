package com.example.gestiondecompras;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.gestiondecompras.models.Gasto;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GastoModelTest {

    @Test
    public void categorias_sonLasOchoEsperadas() {
        assertEquals(8, Gasto.CATEGORIAS.length);
        assertEquals("Comida", Gasto.CAT_COMIDA);
        assertEquals("Transporte", Gasto.CAT_TRANSPORTE);
        assertEquals("Hogar", Gasto.CAT_HOGAR);
        assertEquals("Servicios", Gasto.CAT_SERVICIOS);
        assertEquals("Salud", Gasto.CAT_SALUD);
        assertEquals("Entretenimiento", Gasto.CAT_ENTRETENIMIENTO);
        assertEquals("Compras", Gasto.CAT_COMPRAS);
        assertEquals("Otros", Gasto.CAT_OTROS);
    }

    @Test
    public void categorias_sinDuplicados() {
        Set<String> unicas = new HashSet<>(Arrays.asList(Gasto.CATEGORIAS));
        assertEquals(Gasto.CATEGORIAS.length, unicas.size());
        assertTrue(unicas.contains(Gasto.CAT_COMIDA));
        assertTrue(unicas.contains(Gasto.CAT_OTROS));
    }

    @Test
    public void gastoNuevo_valoresPorDefecto() {
        Gasto g = new Gasto();
        assertEquals(0L, g.getId());
        assertEquals(0.0, g.getMonto(), 0.001);
        assertEquals(0L, g.getFechaEpoch());
    }

    @Test
    public void gastoCompleto_roundtripDatos() {
        Gasto g = new Gasto();
        g.setCategoria(Gasto.CAT_COMIDA);
        g.setDescripcion("Mercado");
        g.setMonto(1250.75);
        g.setFechaEpoch(1780000000000L);
        g.setNotas("Semanal");

        assertEquals(Gasto.CAT_COMIDA, g.getCategoria());
        assertEquals("Mercado", g.getDescripcion());
        assertEquals(1250.75, g.getMonto(), 0.001);
        assertEquals(1780000000000L, g.getFechaEpoch());
        assertEquals("Semanal", g.getNotas());
    }
}