package com.example.gestiondecompras.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Tarjeta;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NuevaTarjetaViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executorService;

    public NuevaTarjetaViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void saveTarjeta(Tarjeta tarjeta) {
        executorService.execute(() -> {
            if (tarjeta.getId() > 0) {
                db.tarjetaDao().update(tarjeta);
            } else {
                db.tarjetaDao().insert(tarjeta);
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
