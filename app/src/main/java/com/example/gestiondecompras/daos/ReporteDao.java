package com.example.gestiondecompras.daos;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface ReporteDao {
    @Query("SELECT SUM(totalGeneral) FROM pedidos WHERE estado='pagado'")
    Double totalCobrado();

    @Query("SELECT SUM(totalGeneral) FROM pedidos WHERE estado='pendiente'")
    Double totalPendiente();

    @Query("SELECT SUM(montoCompra) FROM pedidos")
    Double ventasGeneradas();

    @Query("SELECT SUM(ganancia) FROM pedidos")
    Double gananciaProyectada();
}
