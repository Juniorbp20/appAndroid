package com.example.gestiondecompras.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tarjetas")
public class Tarjeta {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public String banco;
    public String alias;
    public double limite;
    public double deudaActual;
    public int diaCorte;
    public String fechaVencimiento; // MM-YY
    public String notas;
}