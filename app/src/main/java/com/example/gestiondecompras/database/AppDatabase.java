package com.example.gestiondecompras.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabase.Callback;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.gestiondecompras.daos.ClienteDao;
import com.example.gestiondecompras.daos.PedidoDao;
import com.example.gestiondecompras.daos.ReporteDao;
import com.example.gestiondecompras.daos.TarjetaDao;
import com.example.gestiondecompras.daos.TiendaDao;
import com.example.gestiondecompras.models.Cliente;
import com.example.gestiondecompras.models.Pedido;
import com.example.gestiondecompras.models.Tarjeta;
import com.example.gestiondecompras.models.Tienda;

@Database(
        entities = {
                Cliente.class,
                Tienda.class,
                Tarjeta.class,
                Pedido.class
        },
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    private static final Callback PREPOPULATE_CALLBACK = new Callback() {
        @Override
        public void onCreate(SupportSQLiteDatabase db) {
            super.onCreate(db);
            db.execSQL("INSERT INTO tiendas (nombre) VALUES ('Temu')");
            db.execSQL("INSERT INTO tiendas (nombre) VALUES ('Shein')");
            db.execSQL("INSERT INTO tiendas (nombre) VALUES ('Amazon')");
            db.execSQL("INSERT INTO tiendas (nombre) VALUES ('EBay')");
            db.execSQL("INSERT INTO tiendas (nombre) VALUES ('AliExpress')");
            db.execSQL("INSERT INTO tiendas (nombre) VALUES ('Alibaba')");
        }
    };

    public static AppDatabase getInstance(final android.content.Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "GestionCompras.db")
                            .fallbackToDestructiveMigration()
                            .addCallback(PREPOPULATE_CALLBACK)
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
