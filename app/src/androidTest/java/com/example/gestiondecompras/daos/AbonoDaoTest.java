package com.example.gestiondecompras.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Abono;
import com.example.gestiondecompras.models.Cliente;
import com.example.gestiondecompras.models.Pedido;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class AbonoDaoTest {

    private AppDatabase db;
    private AbonoDao dao;
    private long clienteId;

    @Before
    public void setUp() {
        db = TestDb.crear();
        dao = db.abonoDao();

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

    private long nuevoPedido(String estado, double total) {
        Pedido p = new Pedido();
        p.clienteId = clienteId;
        p.clienteNombre = "Ana Lopez";
        p.montoCompra = total * 0.6;
        p.ganancia = total * 0.4;
        p.totalGeneral = total;
        p.fechaRegistroEpoch = System.currentTimeMillis();
        p.estado = estado;
        return db.pedidoDao().insert(p);
    }

    private Abono nuevoAbono(long pedidoId, double monto, long fecha) {
        Abono abono = new Abono();
        abono.pedidoId = pedidoId;
        abono.monto = monto;
        abono.fechaEpoch = fecha;
        abono.metodo = Abono.METODO_EFECTIVO;
        return abono;
    }

    @Test
    public void insertYTotalPorPedido() {
        long pedidoId = nuevoPedido(Pedido.ESTADO_PENDIENTE, 1000.0);
        dao.insert(nuevoAbono(pedidoId, 300.0, 1000L));
        dao.insert(nuevoAbono(pedidoId, 200.0, 2000L));

        assertEquals(500.0, dao.getTotalAbonado(pedidoId), 0.001);
        List<Abono> abonos = dao.getAbonosPorPedido(pedidoId);
        assertEquals(2, abonos.size());
        assertEquals(200.0, abonos.get(0).getMonto(), 0.001);
    }

    @Test
    public void delete_reduceElTotal() {
        long pedidoId = nuevoPedido(Pedido.ESTADO_PENDIENTE, 1000.0);
        Abono abono = nuevoAbono(pedidoId, 300.0, 1000L);
        abono.id = dao.insert(abono);

        assertEquals(1, dao.delete(abono));
        assertEquals(0.0, dao.getTotalAbonado(pedidoId), 0.001);
    }

    @Test
    public void borrarPedido_eliminaSusAbonos() {
        long pedidoId = nuevoPedido(Pedido.ESTADO_PENDIENTE, 1000.0);
        dao.insert(nuevoAbono(pedidoId, 300.0, 1000L));

        Pedido pedido = db.pedidoDao().getPedidoById(pedidoId);
        assertEquals(1, db.pedidoDao().delete(pedido));
        assertTrue(dao.getAbonosPorPedido(pedidoId).isEmpty());
    }

    @Test
    public void totalRangoYGeneral_filtranFechasYCancelados() {
        long activo = nuevoPedido(Pedido.ESTADO_PENDIENTE, 1000.0);
        long cancelado = nuevoPedido(Pedido.ESTADO_CANCELADO, 500.0);
        dao.insert(nuevoAbono(activo, 300.0, 1000L));
        dao.insert(nuevoAbono(activo, 200.0, 5000L));
        dao.insert(nuevoAbono(cancelado, 500.0, 1000L));

        assertEquals(300.0, dao.getTotalRango(0L, 2000L), 0.001);
        assertEquals(500.0, dao.getTotalRango(0L, 10000L), 0.001);
        assertEquals(500.0, dao.getTotalGeneral(), 0.001);
    }
}
