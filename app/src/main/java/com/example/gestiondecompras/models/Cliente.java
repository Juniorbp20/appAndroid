package com.example.gestiondecompras.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "clientes")
public class Cliente {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String nombre;
    public String apellido;
    public String telefono;
    public String direccion;
    public boolean activo;
    public String email;
}