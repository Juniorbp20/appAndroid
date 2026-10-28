package com.example.gestiondecompras.daos;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
public interface ReporteDao {

    String FILTRO_FECHA = " AND (:desde IS NULL OR fecha_registro_epoch >= :desde) " +
            " AND (:hasta IS NULL OR fecha_registro_epoch <= :hasta) " +
            " AND (:busqueda = '' OR cliente_nombre LIKE '%' || :busqueda || '%') ";

    @Query("SELECT SUM(a.monto) FROM abonos a INNER JOIN pedidos p ON p.id = a.pedido_id " +
            "WHERE p.estado != 'cancelado' " +
            "AND (:desde IS NULL OR p.fecha_registro_epoch >= :desde) " +
            "AND (:hasta IS NULL OR p.fecha_registro_epoch <= :hasta) " +
            "AND (:busqueda = '' OR p.cliente_nombre LIKE '%' || :busqueda || '%')")
    Double totalCobrado(Long desde, Long hasta, String busqueda);

    @Query("SELECT SUM(p.total_general - COALESCE((SELECT SUM(a.monto) FROM abonos a WHERE a.pedido_id = p.id), 0)) " +
            "FROM pedidos p WHERE p.estado != 'pagado' AND p.estado != 'cancelado' " +
            "AND (:desde IS NULL OR p.fecha_registro_epoch >= :desde) " +
            "AND (:hasta IS NULL OR p.fecha_registro_epoch <= :hasta) " +
            "AND (:busqueda = '' OR p.cliente_nombre LIKE '%' || :busqueda || '%')")
    Double totalPendiente(Long desde, Long hasta, String busqueda);

    @Query("SELECT SUM(monto_compra) FROM pedidos WHERE 1=1" + FILTRO_FECHA)
    Double ventasGeneradas(Long desde, Long hasta, String busqueda);

    @Query("SELECT SUM(ganancia) FROM pedidos WHERE 1=1" + FILTRO_FECHA)
    Double gananciaProyectada(Long desde, Long hasta, String busqueda);
}