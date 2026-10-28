package com.example.gestiondecompras.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "abonos",
        foreignKeys = @ForeignKey(
                entity = Pedido.class,
                parentColumns = "id",
                childColumns = "pedido_id",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("pedido_id"))
public class Abono {

    public static final String METODO_EFECTIVO = "Efectivo";
    public static final String METODO_TRANSFERENCIA = "Transferencia";
    public static final String METODO_TARJETA = "Tarjeta";
    public static final String METODO_OTRO = "Otro";

    public static final String[] METODOS = {
            METODO_EFECTIVO, METODO_TRANSFERENCIA, METODO_TARJETA, METODO_OTRO
    };

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "pedido_id")
    public long pedidoId;

    public double monto;

    @ColumnInfo(name = "fecha_epoch")
    public long fechaEpoch;

    public String metodo;

    public String notas;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public long getFechaEpoch() {
        return fechaEpoch;
    }

    public void setFechaEpoch(long fechaEpoch) {
        this.fechaEpoch = fechaEpoch;
    }

    public String getMetodo() {
        return metodo != null ? metodo : METODO_EFECTIVO;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
