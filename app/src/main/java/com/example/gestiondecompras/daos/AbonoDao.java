package com.example.gestiondecompras.daos;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.gestiondecompras.models.Abono;

import java.util.List;

@Dao
public interface AbonoDao {

    @Insert
    long insert(Abono abono);

    @Delete
    int delete(Abono abono);

    @Query("SELECT * FROM abonos WHERE pedido_id = :pedidoId ORDER BY fecha_epoch DESC")
    List<Abono> getAbonosPorPedido(long pedidoId);

    @Query("SELECT COALESCE(SUM(monto), 0) FROM abonos WHERE pedido_id = :pedidoId")
    double getTotalAbonado(long pedidoId);

    @Query("SELECT COALESCE(SUM(a.monto), 0) FROM abonos a " +
            "INNER JOIN pedidos p ON p.id = a.pedido_id " +
            "WHERE p.estado != 'cancelado' AND a.fecha_epoch >= :desde AND a.fecha_epoch <= :hasta")
    double getTotalRango(long desde, long hasta);

    @Query("SELECT COALESCE(SUM(a.monto), 0) FROM abonos a " +
            "INNER JOIN pedidos p ON p.id = a.pedido_id WHERE p.estado != 'cancelado'")
    double getTotalGeneral();
}
