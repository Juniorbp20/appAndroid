package com.example.gestiondecompras.daos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Cliente;
import com.example.gestiondecompras.models.ClienteWithMetrics;
import com.example.gestiondecompras.models.Pedido;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class ClienteDaoTest {

    private AppDatabase db;
    private ClienteDao dao;

    @Before
    public void setUp() {
        db = TestDb.crear();
        dao = db.clienteDao();
    }

    @After
    public void tearDown() {
        TestDb.cerrar(db);
    }

    private Cliente nuevoCliente(String nombre, String apellido, String email, String telefono) {
        Cliente c = new Cliente();
        c.nombre = nombre;
        c.apellido = apellido;
        c.email = email;
        c.telefono = telefono;
        return c;
    }

    @Test
    public void insertYGetAll_clientesGuardados() {
        long id = dao.insert(nuevoCliente("Ana", "Lopez", "ana@test.com", "8090000000"));
        dao.insert(nuevoCliente("Luis", "Perez", "luis@test.com", "8291111111"));
        List<Cliente> clientes = dao.getAllClientes();
        assertEquals(2, clientes.size());
        assertEquals(id, clientes.get(0).id);
        assertEquals("Ana", clientes.get(0).nombre);
    }

    @Test
    public void update_modificaDatos() {
        Cliente c = nuevoCliente("Ana", "Lopez", "ana@test.com", "8090000000");
        long id = dao.insert(c);
        c.id = id;
        c.nombre = "Ana Maria";
        assertEquals(1, dao.update(c));
        Cliente actualizado = dao.getAllClientes().get(0);
        assertEquals("Ana Maria", actualizado.nombre);
        assertEquals("Lopez", actualizado.apellido);
    }

    @Test
    public void delete_eliminaCliente() {
        Cliente c = nuevoCliente("Ana", "Lopez", "ana@test.com", "8090000000");
        long id = dao.insert(c);
        c.id = id;
        assertEquals(1, dao.delete(c));
        assertTrue(dao.getAllClientes().isEmpty());
        assertEquals(0, dao.delete(c));
    }

    @Test
    public void getAllClientesWithMetrics_agregaCantidadYTotal() {
        Cliente c = nuevoCliente("Ana", "Lopez", "ana@test.com", "8090000000");
        long clienteId = dao.insert(c);

        Pedido p1 = new Pedido();
        p1.clienteId = clienteId;
        p1.clienteNombre = "Ana Lopez";
        p1.totalGeneral = 300.0;
        db.pedidoDao().insert(p1);

        Pedido p2 = new Pedido();
        p2.clienteId = clienteId;
        p2.clienteNombre = "Ana Lopez";
        p2.totalGeneral = 200.0;
        db.pedidoDao().insert(p2);

        ClienteDao dao2 = db.clienteDao();
        List<ClienteWithMetrics> metrics = dao2.getAllClientesWithMetrics();
        assertEquals(1, metrics.size());
        assertEquals(2, metrics.get(0).cantidadPedidos);
        assertEquals(500.0, metrics.get(0).totalCompras, 0.001);
    }

    @Test
    public void getAllClientes_devuelveListaVaciaSinDatos() {
        assertTrue(dao.getAllClientes().isEmpty());
        assertNull(db.pedidoDao().getPedidoById(1));
    }
}