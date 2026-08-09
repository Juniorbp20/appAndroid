package com.example.gestiondecompras.daos;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface ReporteDao {

    String FILTRO_FECHA = " AND (:desde IS NULL OR fecha_registro_epoch >= :desde) " +
            " AND (:hasta IS NULL OR fecha_registro_epoch <= :hasta) " +
            " AND (:busqueda = '' OR cliente_nombre LIKE '%' || :busqueda || '%') ";

    @Query("SELECT SUM(total_general) FROM pedidos WHERE estado='pagado'" + FILTRO_FECHA)
    Double totalCobrado(Long desde, Long hasta, String busqueda);

    @Query("SELECT SUM(total_general) FROM pedidos WHERE estado != 'pagado' AND estado != 'cancelado'" + FILTRO_FECHA)
    Double totalPendiente(Long desde, Long hasta, String busqueda);

    @Query("SELECT SUM(monto_compra) FROM pedidos WHERE 1=1" + FILTRO_FECHA)
    Double ventasGeneradas(Long desde, Long hasta, String busqueda);

    @Query("SELECT SUM(ganancia) FROM pedidos WHERE 1=1" + FILTRO_FECHA)
    Double gananciaProyectada(Long desde, Long hasta, String busqueda);
}