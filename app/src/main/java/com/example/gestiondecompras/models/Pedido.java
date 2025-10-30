package com.example.gestiondecompras.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "pedidos",
        foreignKeys = {
                @ForeignKey(entity = Cliente.class, parentColumns = "id", childColumns = "clienteId"),
                @ForeignKey(entity = Tienda.class, parentColumns = "id", childColumns = "tiendaId"),
                @ForeignKey(entity = Tarjeta.class, parentColumns = "id", childColumns = "tarjetaId")
        })
public class Pedido {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long clienteId;
    public long tiendaId;
    public long tarjetaId;
    public double montoCompra;
    public double ganancia;
    public double totalGeneral;
    public long fechaRegistroEpoch;
    public Long fechaEntregaEpoch;
    public String notas;
    @NonNull
    public String estado;

    public static final String ESTADO_PENDIENTE = "pendiente";
    public static final String ESTADO_ENTREGADO = "entregado";
    public static final String ESTADO_PAGADO = "pagado";
    public static final String ESTADO_CANCELADO = "cancelado";
}