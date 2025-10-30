package com.example.gestiondecompras.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.room.Room;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Tarjeta;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NuevaTarjetaViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executorService;

    public NuevaTarjetaViewModel(@NonNull Application application) {
        super(application);
        db = Room.databaseBuilder(application, AppDatabase.class, "GestionCompras.db").build();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void saveTarjeta(Tarjeta tarjeta) {
        executorService.execute(() -> {
            db.tarjetaDao().insert(tarjeta);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
