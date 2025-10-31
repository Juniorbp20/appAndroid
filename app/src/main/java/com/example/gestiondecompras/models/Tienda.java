package com.example.gestiondecompras.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tiendas")
public class Tienda {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String nombre;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre != null ? nombre : "";
    }
}
