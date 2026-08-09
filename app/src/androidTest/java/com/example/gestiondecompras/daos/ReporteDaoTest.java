package com.example.gestiondecompras.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Cliente;
import com.example.gestiondecompras.models.Pedido;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ReporteDaoTest {

    private static final long HOY = System.currentTimeMillis();

    private AppDatabase db;
    private ReporteDao dao;
    private long clienteId;

    @Before
    public void setUp() {
        db = TestDb.crear();
        dao = db.reporteDao();

        Cliente cliente = new Cliente();
        cliente.nombre = "Ana";
        cliente.apellido = "Lopez";
        cliente.email = "ana@test.com";
        cliente.telefono = "8090000000";
        clienteId = db.clienteDao().insert(cliente);
    }

    @After
    public void tearDown() {
        TestDb.cerrar(db);
    }

    private Pedido nuevoPedido(String estado, double total, Long epoch) {
        Pedido p = new Pedido();
        p.clienteId = clienteId;
        p.clienteNombre = "Ana Lopez";
        p.montoCompra = total * 0.6;
        p.ganancia = total * 0.4;
        p.totalGeneral = total;
        p.fechaRegistroEpoch = epoch != null ? epoch : HOY;
        p.estado = estado;
        return p;
    }

    @Test
    public void totalCobrado_soloPagados() {
        db.pedidoDao().insert(nuevoPedido(Pedido.ESTADO_PAGADO, 100.0, null));
        db.pedidoDao().insert(nuevoPedido(Pedido.ESTADO_PAGADO, 50.0, null));
        db.pedidoDao().insert(nuevoPedido(Pedido.ESTADO_PENDIENTE, 80.0, null));
        db.pedidoDao().insert(nuevoPedido(Pedido.ESTADO_CANCELADO, 30.0, null));

        assertEquals(150.0, dao.totalCobrado(null, null, ""), 0.001);
        assertEquals(80.0, dao.totalPendiente(null, null, ""), 0.001);
        assertEquals(156.0, dao.ventasGeneradas(null, null, ""), 0.001);
        assertEquals(104.0, dao.gananciaProyectada(null, null, ""), 0.001);
    }

    @Test
    public void totalesFiltrados_porRangoYBusqueda() {
        db.pedidoDao().insert(nuevoPedido(Pedido.ESTADO_PAGADO, 100.0, HOY - 5_000_000_000L));
        db.pedidoDao().insert(nuevoPedido(Pedido.ESTADO_PAGADO, 50.0, HOY));

        assertEquals(150.0, dao.totalCobrado(null, null, ""), 0.001);
        assertEquals(50.0, dao.totalCobrado(HOY - 1000L, null, ""), 0.001);
        assertEquals(100.0, dao.totalCobrado(null, HOY - 1_000_000_000L, ""), 0.001);
        assertEquals(150.0, dao.totalCobrado(null, null, "Ana"), 0.001);
        assertNull(dao.totalCobrado(null, null, "Inexistente"));
    }

    @Test
    public void sinDatos_devuelveNull() {
        assertNull(dao.totalCobrado(null, null, ""));
        assertNull(dao.totalPendiente(null, null, ""));
        assertNull(dao.ventasGeneradas(null, null, ""));
        assertNull(dao.gananciaProyectada(null, null, ""));
    }
}