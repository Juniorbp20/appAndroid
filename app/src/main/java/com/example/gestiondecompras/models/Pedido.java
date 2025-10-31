package com.example.gestiondecompras.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "pedidos",
        foreignKeys = {
                @ForeignKey(entity = Cliente.class, parentColumns = "id", childColumns = "cliente_id"),
                @ForeignKey(entity = Tienda.class, parentColumns = "id", childColumns = "tienda_id"),
                @ForeignKey(entity = Tarjeta.class, parentColumns = "id", childColumns = "tarjeta_rel_id")
        })
public class Pedido {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "cliente_id")
    public long clienteId;
    @ColumnInfo(name = "tienda_id")
    public Long tiendaId;
    @ColumnInfo(name = "tarjeta_rel_id")
    public Long tarjetaId;
    @ColumnInfo(name = "cliente_nombre", defaultValue = "")
    public String clienteNombre;
    @ColumnInfo(name = "tienda", defaultValue = "")
    public String tiendaNombre;
    @ColumnInfo(name = "tarjeta_alias", defaultValue = "")
    public String tarjetaAlias;
    @ColumnInfo(name = "monto_compra")
    public double montoCompra;
    public double ganancia;
    @ColumnInfo(name = "total_general")
    public double totalGeneral;
    @ColumnInfo(name = "fecha_registro_epoch", defaultValue = "0")
    public Long fechaRegistroEpoch = 0L;
    @ColumnInfo(name = "fecha_entrega")
    public Long fechaEntregaEpoch;
    public String notas;
    @NonNull
    @ColumnInfo(name = "estado", defaultValue = ESTADO_PENDIENTE)
    public String estado = ESTADO_PENDIENTE;

    public static final String ESTADO_PENDIENTE = "pendiente";
    public static final String ESTADO_ENTREGADO = "entregado";
    public static final String ESTADO_PAGADO = "pagado";
    public static final String ESTADO_CANCELADO = "cancelado";

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getClienteId() {
        return clienteId;
    }

    public void setClienteId(long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getTiendaId() {
        return tiendaId;
    }

    public void setTiendaId(Long tiendaId) {
        this.tiendaId = tiendaId;
    }

    public Long getTarjetaId() {
        return tarjetaId;
    }

    public void setTarjetaId(Long tarjetaId) {
        this.tarjetaId = tarjetaId;
    }

    public String getClienteNombre() {
        return clienteNombre != null ? clienteNombre : "";
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getTienda() {
        return tiendaNombre != null ? tiendaNombre : "";
    }

    public void setTienda(String tiendaNombre) {
        this.tiendaNombre = tiendaNombre;
    }

    public String getTarjetaAlias() {
        return tarjetaAlias;
    }

    public void setTarjetaAlias(String tarjetaAlias) {
        this.tarjetaAlias = tarjetaAlias;
    }

    public double getMontoCompra() {
        return montoCompra;
    }

    public void setMontoCompra(double montoCompra) {
        this.montoCompra = montoCompra;
    }

    public double getGanancia() {
        return ganancia;
    }

    public void setGanancia(double ganancia) {
        this.ganancia = ganancia;
    }

    public double getTotalGeneral() {
        return totalGeneral;
    }

    public void setTotalGeneral(double totalGeneral) {
        this.totalGeneral = totalGeneral;
    }

    public Long getFechaRegistroEpoch() {
        return fechaRegistroEpoch;
    }

    public void setFechaRegistroEpoch(Long fechaRegistroEpoch) {
        this.fechaRegistroEpoch = fechaRegistroEpoch;
    }

    public Date getFechaRegistro() {
        return fechaRegistroEpoch != null ? new Date(fechaRegistroEpoch) : null;
    }

    public Long getFechaEntregaEpoch() {
        return fechaEntregaEpoch;
    }

    public void setFechaEntregaEpoch(Long fechaEntregaEpoch) {
        this.fechaEntregaEpoch = fechaEntregaEpoch;
    }

    public Date getFechaEntrega() {
        return fechaEntregaEpoch != null ? new Date(fechaEntregaEpoch) : null;
    }

    public void setFechaEntrega(Date fechaEntrega) {
        this.fechaEntregaEpoch = fechaEntrega != null ? fechaEntrega.getTime() : null;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    @NonNull
    public String getEstado() {
        return estado;
    }

    public void setEstado(@NonNull String estado) {
        this.estado = estado;
    }
}
