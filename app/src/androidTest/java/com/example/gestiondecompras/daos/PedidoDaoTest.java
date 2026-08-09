package com.example.gestiondecompras.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Cliente;
import com.example.gestiondecompras.models.ClienteGanancia;
import com.example.gestiondecompras.models.DashboardRow;
import com.example.gestiondecompras.models.Pedido;
import com.example.gestiondecompras.models.Tarjeta;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class PedidoDaoTest {

    private AppDatabase db;
    private PedidoDao dao;
    private long clienteId;
    private long tarjetaId;

    @Before
    public void setUp() {
        db = TestDb.crear();
        dao = db.pedidoDao();

        Cliente cliente = new Cliente();
        cliente.nombre = "Ana";
        cliente.apellido = "Lopez";
        cliente.email = "ana@test.com";
        cliente.telefono = "8090000000";
        clienteId = db.clienteDao().insert(cliente);

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.banco = "Popular";
        tarjeta.alias = "Visa";
        tarjeta.limite = 50000.0;
        tarjeta.deudaActual = 1000.0;
        tarjeta.diaCorte = 15;
        tarjetaId = db.tarjetaDao().insert(tarjeta);
    }

    @After
    public void tearDown() {
        TestDb.cerrar(db);
    }

    private Pedido nuevoPedido(String estado, String clienteNombre, double total, Long entregaEpoch) {
        Pedido p = new Pedido();
        p.clienteId = clienteId;
        p.clienteNombre = clienteNombre;
        p.montoCompra = total * 0.6;
        p.ganancia = total * 0.4;
        p.totalGeneral = total;
        p.fechaRegistroEpoch = System.currentTimeMillis();
        p.fechaEntregaEpoch = entregaEpoch;
        p.estado = estado;
        return p;
    }

    @Test
    public void insertYGetById_roundtripCompleto() {
        long id = dao.insert(nuevoPedido(Pedido.ESTADO_PENDIENTE, "Ana Lopez", 500.0, null));
        Pedido obt = dao.getPedidoById(id);
        assertNotNull(obt);
        assertEquals("Ana Lopez", obt.getClienteNombre());
        assertEquals(500.0, obt.totalGeneral, 0.001);
        assertEquals(200.0, obt.ganancia, 0.001);
        assertEquals(Pedido.ESTADO_PENDIENTE, obt.estado);
        assertNull(dao.getPedidoById(999));
    }

    @Test
    public void updateYDelete_reflejanCambios() {
        long id = dao.insert(nuevoPedido(Pedido.ESTADO_PENDIENTE, "Ana Lopez", 100.0, null));
        Pedido p = dao.getPedidoById(id);
        p.estado = Pedido.ESTADO_PAGADO;
        assertEquals(1, dao.update(p));
        assertEquals(Pedido.ESTADO_PAGADO, dao.getPedidoById(id).estado);
        assertEquals(1, dao.delete(p));
        assertNull(dao.getPedidoById(id));
    }

    @Test
    public void getPedidosFiltrados_porEstadoBusquedaYCliente() {
        dao.insert(nuevoPedido(Pedido.ESTADO_PENDIENTE, "Ana Lopez", 100.0, null));
        dao.insert(nuevoPedido(Pedido.ESTADO_PAGADO, "Luis Perez", 200.0, null));
        dao.insert(nuevoPedido(Pedido.ESTADO_ENTREGADO, "Ana Lopez", 300.0, null));

        Cliente otro = new Cliente();
        otro.nombre = "Otro";
        otro.apellido = "Cliente";
        otro.email = "otro@test.com";
        otro.telefono = "8291111111";
        long otroId = db.clienteDao().insert(otro);

        Pedido deOtro = nuevoPedido(Pedido.ESTADO_PENDIENTE, "Otro Cliente", 50.0, null);
        deOtro.clienteId = otroId;
        deOtro.clienteNombre = "Otro Cliente";
        dao.insert(deOtro);

        assertEquals(1, dao.getPedidosFiltrados(Pedido.ESTADO_PAGADO, "", null).size());
        assertEquals(1, dao.getPedidosFiltrados("", "Luis", null).size());
        assertEquals(0, dao.getPedidosFiltrados("", "inexistente", null).size());
        assertEquals(3, dao.getPedidosFiltrados("", "", (int) clienteId).size());
        assertEquals(1, dao.getPedidosFiltrados("", "", (int) otroId).size());
        assertEquals(4, dao.getPedidosFiltrados("", "", null).size());
    }

    @Test
    public void pedidosPorDia_yEntregasPorDia_filtranPorFecha() {
        long hoy = System.currentTimeMillis();
        Pedido p3 = nuevoPedido(Pedido.ESTADO_PENDIENTE, "Carlos Diaz", 300.0, hoy - 2_000_000_000L);
        p3.fechaRegistroEpoch = hoy - 2_000_000_000L;
        dao.insert(nuevoPedido(Pedido.ESTADO_PENDIENTE, "Ana Lopez", 100.0, hoy));
        dao.insert(nuevoPedido(Pedido.ESTADO_PENDIENTE, "Luis Perez", 200.0, hoy));
        dao.insert(p3);

        assertEquals(2, dao.pedidosPorDia(hoy).size());
        assertEquals(2, dao.pedidosPorDiaYCliente(hoy, "").size());
        assertEquals(1, dao.pedidosPorDiaYCliente(hoy, "Ana").size());
        assertEquals(2, dao.getEntregasPorDia(hoy).size());
    }

    @Test
    public void findByEstado_yGetPedidosNoPagados() {
        dao.insert(nuevoPedido(Pedido.ESTADO_PENDIENTE, "Ana Lopez", 100.0, null));
        dao.insert(nuevoPedido(Pedido.ESTADO_PAGADO, "Luis Perez", 200.0, null));
        dao.insert(nuevoPedido(Pedido.ESTADO_CANCELADO, "Carlos Diaz", 300.0, null));

        assertEquals(1, dao.findByEstado(Pedido.ESTADO_PAGADO).size());
        assertEquals(3, dao.findByEstado("").size());
        assertEquals(1, dao.getPedidosNoPagados().size());
    }

    @Test
    public void getDashboard_acumulaTotales() {
        Pedido ayer = nuevoPedido(Pedido.ESTADO_PENDIENTE, "Ana Lopez", 100.0, null);
        ayer.fechaRegistroEpoch = System.currentTimeMillis() - 2 * 86_400_000L;
        dao.insert(ayer);
        dao.insert(nuevoPedido(Pedido.ESTADO_PENDIENTE, "Ana Lopez", 100.0, null));
        dao.insert(nuevoPedido(Pedido.ESTADO_PAGADO, "Luis Perez", 50.0, null));
        dao.insert(nuevoPedido(Pedido.ESTADO_CANCELADO, "Carlos Diaz", 30.0, null));

        DashboardRow row = dao.getDashboard();
        assertNotNull(row);
        assertEquals(200.0, row.totalPendiente, 0.001);
        assertEquals(50.0, row.totalPagado, 0.001);
        assertEquals(3, row.pedidosHoy);
    }

    @Test
    public void getGananciasPorCliente_sumaPorCliente() {
        dao.insert(nuevoPedido(Pedido.ESTADO_PENDIENTE, "Ana Lopez", 100.0, null));
        dao.insert(nuevoPedido(Pedido.ESTADO_PAGADO, "Ana Lopez", 100.0, null));
        dao.insert(nuevoPedido(Pedido.ESTADO_CANCELADO, "Ana Lopez", 100.0, null));

        List<ClienteGanancia> ganancias = dao.getGananciasPorCliente();
        assertEquals(1, ganancias.size());
        assertEquals("Ana Lopez", ganancias.get(0).clienteNombre);
        assertEquals(80.0, ganancias.get(0).totalGanancia, 0.001);
    }

    @Test
    public void atrasadosYProximos_detectanFechas() {
        long hoy = System.currentTimeMillis();
        long ayer = hoy - 24 * 60 * 60 * 1000L;
        long manana = hoy + 24 * 60 * 60 * 1000L;

        dao.insert(nuevoPedido(Pedido.ESTADO_PENDIENTE, "Atrasado", 100.0, ayer));
        dao.insert(nuevoPedido(Pedido.ESTADO_PENDIENTE, "Proximo", 200.0, manana));
        dao.insert(nuevoPedido(Pedido.ESTADO_PAGADO, "Pagado viejo", 300.0, ayer));
        dao.insert(nuevoPedido(Pedido.ESTADO_PENDIENTE, "Sin fecha", 400.0, null));

        assertEquals(1, dao.getOverdueOrdersCount(hoy));
        assertEquals(1, dao.getPedidosAtrasados(hoy).size());
        assertEquals("Atrasado", dao.getPedidosAtrasados(hoy).get(0).getClienteNombre());
        assertEquals(1, dao.getProximosPedidos(hoy, 10).size());
        assertEquals(3, dao.getProximosPedidos(0L, 10).size());
    }

    @Test
    public void porCobrar_yPagados_conRangoYBusqueda() {
        long hoy = System.currentTimeMillis();
        dao.insert(nuevoPedido(Pedido.ESTADO_PENDIENTE, "Ana Lopez", 100.0, null));
        dao.insert(nuevoPedido(Pedido.ESTADO_ENTREGADO, "Luis Perez", 200.0, null));
        dao.insert(nuevoPedido(Pedido.ESTADO_PAGADO, "Carlos Diaz", 300.0, null));
        dao.insert(nuevoPedido(Pedido.ESTADO_CANCELADO, "Diana Ruiz", 400.0, null));

        assertEquals(2, dao.getPedidosPorCobrar(null, null, "").size());
        assertEquals(1, dao.getPedidosPorCobrar(null, null, "Luis").size());
        assertEquals(1, dao.getPedidosPorCobrar(hoy + 1, null, "").size());
        assertEquals(1, dao.getPedidosPagadosFiltrados(null, null, "").size());
        assertEquals(1, dao.getPedidosPagados().size());
    }

    @Test
    public void countPedidosPorTarjeta_cuentaPendientes() {
        Pedido p = nuevoPedido(Pedido.ESTADO_PENDIENTE, "Ana Lopez", 100.0, null);
        p.tarjetaId = tarjetaId;
        dao.insert(p);
        p = nuevoPedido(Pedido.ESTADO_PAGADO, "Ana Lopez", 200.0, null);
        p.tarjetaId = tarjetaId;
        dao.insert(p);

        assertEquals(1, dao.countPedidosPorTarjeta(tarjetaId));
        assertEquals(0, dao.countPedidosPorTarjeta(999));
    }
}