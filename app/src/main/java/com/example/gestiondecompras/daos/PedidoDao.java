package com.example.gestiondecompras.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gestiondecompras.models.DashboardRow;
import com.example.gestiondecompras.models.Pedido;

import java.util.List;

@Dao
public interface PedidoDao {
    @Insert
    long insert(Pedido p);

    @Update
    int update(Pedido p);

    @Delete
    int delete(Pedido p);

    @Query("SELECT * FROM pedidos WHERE id = :id LIMIT 1")
    Pedido getPedidoById(long id);

    @Query("SELECT * FROM pedidos WHERE (:estado = '' OR estado = :estado) AND (:busqueda = '' OR cliente_nombre LIKE '%' || :busqueda || '%') AND (:clienteId IS NULL OR cliente_id = :clienteId) ORDER BY fecha_registro_epoch DESC")
    List<Pedido> getPedidosFiltrados(String estado, String busqueda, Integer clienteId);

    @Query("SELECT * FROM pedidos WHERE date(fecha_registro_epoch/1000,'unixepoch')=date(:epoch/1000,'unixepoch') ORDER BY fecha_registro_epoch ASC")
    List<Pedido> pedidosPorDia(long epoch);

    @Query("SELECT * FROM pedidos WHERE (:estado = '' OR estado = :estado) ORDER BY fecha_registro_epoch DESC")
    List<Pedido> findByEstado(String estado);

    @Query("SELECT SUM(CASE WHEN estado != 'pagado' AND estado != 'cancelado' THEN total_general ELSE 0 END) AS totalPendiente, " +
           "SUM(CASE WHEN estado = 'pagado' THEN total_general ELSE 0 END) AS totalPagado, " +
           "COUNT(CASE WHEN date(fecha_registro_epoch/1000,'unixepoch')=date('now') THEN 1 END) AS pedidosHoy " +
           "FROM pedidos")
    DashboardRow getDashboard();

    @Query("SELECT cliente_nombre AS clienteNombre, SUM(ganancia) AS totalGanancia FROM pedidos WHERE estado != 'cancelado' GROUP BY cliente_id ORDER BY totalGanancia DESC")
    List<com.example.gestiondecompras.models.ClienteGanancia> getGananciasPorCliente();

    @Query("SELECT COUNT(*) FROM pedidos WHERE estado = 'pendiente' AND fecha_entrega < :todayEpoch")
    int getOverdueOrdersCount(long todayEpoch);

    @Query("SELECT * FROM pedidos WHERE estado = 'pendiente' AND fecha_entrega IS NOT NULL AND fecha_entrega < :todayEpoch ORDER BY fecha_entrega ASC")
    List<Pedido> getPedidosAtrasados(long todayEpoch);

    @Query("SELECT * FROM pedidos WHERE fecha_entrega IS NOT NULL AND fecha_entrega >= :fromEpoch ORDER BY fecha_entrega ASC LIMIT :limit")
    List<Pedido> getProximosPedidos(long fromEpoch, int limit);

    @Query("SELECT * FROM pedidos WHERE estado = 'pagado' ORDER BY fecha_registro_epoch DESC")
    List<Pedido> getPedidosPagados();

    @Query("SELECT COUNT(*) FROM pedidos WHERE tarjeta_rel_id = :tarjetaId AND estado = 'pendiente'")
    int countPedidosPorTarjeta(long tarjetaId);

    @Query("SELECT * FROM pedidos WHERE estado != 'pagado' AND estado != 'cancelado' ORDER BY fecha_registro_epoch DESC")
    List<Pedido> getPedidosNoPagados();
}
