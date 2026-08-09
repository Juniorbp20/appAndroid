package com.example.gestiondecompras.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "gastos")
public class Gasto {
    public static final String CAT_COMIDA = "Comida";
    public static final String CAT_TRANSPORTE = "Transporte";
    public static final String CAT_HOGAR = "Hogar";
    public static final String CAT_SERVICIOS = "Servicios";
    public static final String CAT_SALUD = "Salud";
    public static final String CAT_ENTRETENIMIENTO = "Entretenimiento";
    public static final String CAT_COMPRAS = "Compras";
    public static final String CAT_OTROS = "Otros";

    public static final String[] CATEGORIAS = {
            CAT_COMIDA, CAT_TRANSPORTE, CAT_HOGAR, CAT_SERVICIOS,
            CAT_SALUD, CAT_ENTRETENIMIENTO, CAT_COMPRAS, CAT_OTROS
    };

    @PrimaryKey(autoGenerate = true)
    public long id;
    public String categoria;
    public String descripcion;
    public double monto;
    public long fechaEpoch;
    public String notas;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
