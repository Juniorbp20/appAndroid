package com.example.gestiondecompras.models;

import androidx.room.Embedded;

public class ClienteWithMetrics extends Cliente {
    public int cantidadPedidos;
    public double totalCompras;

    // Default constructor
    public ClienteWithMetrics() {
        super();
    }
}
