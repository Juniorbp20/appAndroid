package com.example.gestiondecompras.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Tienda;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class TiendaDaoTest {

    private AppDatabase db;
    private TiendaDao dao;

    @Before
    public void setUp() {
        db = TestDb.crear();
        dao = db.tiendaDao();
    }

    @After
    public void tearDown() {
        TestDb.cerrar(db);
    }

    @Test
    public void insertYGetAll_tiendasGuardadas() {
        long id = dao.insert(crearTienda("Temu"));
        dao.insert(crearTienda("Shein"));
        List<Tienda> tiendas = dao.getAllTiendas();
        assertEquals(2, tiendas.size());
        assertEquals(id, tiendas.get(0).getId());
        assertEquals("Temu", tiendas.get(0).getNombre());
    }

    @Test
    public void update_modificaNombre() {
        Tienda t = crearTienda("Temu");
        long id = dao.insert(t);
        t.setId(id);
        t.setNombre("Temu Oficial");
        assertEquals(1, dao.update(t));
        assertEquals("Temu Oficial", dao.getAllTiendas().get(0).getNombre());
    }

    @Test
    public void delete_eliminaTienda() {
        Tienda t = crearTienda("Amazon");
        long id = dao.insert(t);
        t.setId(id);
        assertEquals(1, dao.delete(t));
        assertTrue(dao.getAllTiendas().isEmpty());
    }

    @Test
    public void toString_devuelveNombre() {
        Tienda t = crearTienda("AliExpress");
        assertEquals("AliExpress", t.toString());
        t.nombre = null;
        assertEquals("", t.toString());
    }

    private Tienda crearTienda(String nombre) {
        Tienda t = new Tienda();
        t.setNombre(nombre);
        return t;
    }
}