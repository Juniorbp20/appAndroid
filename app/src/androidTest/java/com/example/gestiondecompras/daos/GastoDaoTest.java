package com.example.gestiondecompras.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Gasto;
import com.example.gestiondecompras.models.GastoCategoria;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class GastoDaoTest {

    private static final long DIA_1 = 1780000000000L;
    private static final long DIA_2 = 1780000000000L + 86_400_000L;
    private static final long DIA_3 = 1780000000000L + 2 * 86_400_000L;

    private AppDatabase db;
    private GastoDao dao;

    @Before
    public void setUp() {
        db = TestDb.crear();
        dao = db.gastoDao();
    }

    @After
    public void tearDown() {
        TestDb.cerrar(db);
    }

    private Gasto nuevoGasto(String categoria, String descripcion, double monto, long epoch) {
        Gasto g = new Gasto();
        g.categoria = categoria;
        g.descripcion = descripcion;
        g.monto = monto;
        g.fechaEpoch = epoch;
        return g;
    }

    @Test
    public void insertYGetAll_ordenaDescPorFecha() {
        dao.insert(nuevoGasto(Gasto.CAT_COMIDA, "Mercado", 100.0, DIA_1));
        dao.insert(nuevoGasto(Gasto.CAT_TRANSPORTE, "Gasolina", 50.0, DIA_3));
        List<Gasto> gastos = dao.getAllGastos();
        assertEquals(2, gastos.size());
        assertEquals("Gasolina", gastos.get(0).descripcion);
        assertEquals("Mercado", gastos.get(1).descripcion);
    }

    @Test
    public void updateYDelete_reflejanCambios() {
        long id = dao.insert(nuevoGasto(Gasto.CAT_COMIDA, "Mercado", 100.0, DIA_1));
        Gasto g = dao.getAllGastos().get(0);
        g.monto = 150.0;
        assertEquals(1, dao.update(g));
        assertEquals(150.0, dao.getAllGastos().get(0).monto, 0.001);
        assertEquals(1, dao.delete(g));
        assertTrue(dao.getAllGastos().isEmpty());
    }

    @Test
    public void getGastosFiltrados_rangoYBusqueda() {
        dao.insert(nuevoGasto(Gasto.CAT_COMIDA, "Mercado semanal", 100.0, DIA_1));
        dao.insert(nuevoGasto(Gasto.CAT_TRANSPORTE, "Gasolina", 50.0, DIA_2));
        dao.insert(nuevoGasto(Gasto.CAT_COMIDA, "Almuerzo", 25.0, DIA_3));

        assertEquals(3, dao.getGastosFiltrados(null, null, "").size());
        assertEquals(2, dao.getGastosFiltrados(DIA_2, DIA_3, "").size());
        assertEquals(1, dao.getGastosFiltrados(DIA_3, null, "").size());
        assertEquals(1, dao.getGastosFiltrados(null, null, "me").size());
        assertEquals(0, dao.getGastosFiltrados(null, null, "nada").size());
    }

    @Test
    public void getTotalRango_sumaMontosDelRango() {
        dao.insert(nuevoGasto(Gasto.CAT_COMIDA, "Mercado", 100.0, DIA_1));
        dao.insert(nuevoGasto(Gasto.CAT_TRANSPORTE, "Gasolina", 50.0, DIA_2));
        dao.insert(nuevoGasto(Gasto.CAT_COMIDA, "Almuerzo", 25.0, DIA_3));

        assertEquals(175.0, dao.getTotalRango(DIA_1, DIA_3), 0.001);
        assertEquals(75.0, dao.getTotalRango(DIA_2, DIA_3), 0.001);
        assertEquals(0.0, dao.getTotalRango(DIA_3 + 1, DIA_3 + 2), 0.001);
    }

    @Test
    public void getTotalesPorCategoria_agrupaYOrdena() {
        dao.insert(nuevoGasto(Gasto.CAT_COMIDA, "Mercado", 100.0, DIA_1));
        dao.insert(nuevoGasto(Gasto.CAT_TRANSPORTE, "Gasolina", 50.0, DIA_2));
        dao.insert(nuevoGasto(Gasto.CAT_COMIDA, "Almuerzo", 25.0, DIA_3));

        List<GastoCategoria> categorias = dao.getTotalesPorCategoria(DIA_1, DIA_3);
        assertEquals(2, categorias.size());
        assertEquals("Comida", categorias.get(0).categoria);
        assertEquals(125.0, categorias.get(0).total, 0.001);
        assertEquals(2, categorias.get(0).cantidad);
        assertEquals("Transporte", categorias.get(1).categoria);
        assertEquals(50.0, categorias.get(1).total, 0.001);
    }
}