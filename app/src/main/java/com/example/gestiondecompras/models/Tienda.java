package com.example.gestiondecompras.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tiendas")
public class Tienda {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String nombre;
}