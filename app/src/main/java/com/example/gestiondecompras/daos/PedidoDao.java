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

    @Query("SELECT * FROM pedidos WHERE (:estado = '' OR estado = :estado) AND (:busqueda = '' OR clienteNombre LIKE '%' || :busqueda || '%') AND (:clienteId IS NULL OR clienteId = :clienteId)")
    List<Pedido> getPedidosFiltrados(String estado, String busqueda, Integer clienteId);

    @Query("SELECT * FROM Pedido WHERE date(fechaEntregaEpoch/1000,'unixepoch')=date(:epoch/1000,'unixepoch')")
    List<Pedido> pedidosPorDia(long epoch);

    @Query("SELECT SUM(CASE WHEN estado='pendiente' THEN totalGeneral ELSE 0 END) AS totalPendiente, " +
           "COUNT(CASE WHEN date(fechaRegistroEpoch/1000,\'unixepoch\')=date(\'now\') THEN 1 END) AS pedidosHoy " +
           "FROM pedidos")
    DashboardRow getDashboard();

    @Query("SELECT COUNT(*) FROM pedidos WHERE estado = 'pendiente' AND fechaEntregaEpoch < :todayEpoch")
    int getOverdueOrdersCount(long todayEpoch);
}
