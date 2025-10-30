package com.example.gestiondecompras.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.gestiondecompras.daos.ClienteDao;
import com.example.gestiondecompras.daos.PedidoDao;
import com.example.gestiondecompras.daos.ReporteDao;
import com.example.gestiondecompras.daos.TarjetaDao;
import com.example.gestiondecompras.daos.TiendaDao;
import com.example.gestiondecompras.models.Cliente;
import com.example.gestiondecompras.models.Pedido;
import com.example.gestiondecompras.models.Tarjeta;
import com.example.gestiondecompras.models.Tienda;

public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(final android.content.Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = androidx.room.Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "GestionCompras.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract ClienteDao clienteDao();
    public abstract TiendaDao tiendaDao();
    public abstract TarjetaDao tarjetaDao();
    public abstract PedidoDao pedidoDao();
    public abstract ReporteDao reporteDao();
}
