package com.example.gestiondecompras.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Tarjeta;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListaTarjetasViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executorService;

    private final MutableLiveData<List<Tarjeta>> tarjetas = new MutableLiveData<>();

    public ListaTarjetasViewModel(@NonNull Application application) {
        super(application);
        db = Room.databaseBuilder(application, AppDatabase.class, "GestionCompras.db").build();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Tarjeta>> getTarjetas() {
        return tarjetas;
    }

    public void loadTarjetas() {
        executorService.execute(() -> {
            tarjetas.postValue(db.tarjetaDao().getAllTarjetas());
        });
    }

    public void deleteTarjeta(Tarjeta tarjeta) {
        executorService.execute(() -> {
            db.tarjetaDao().delete(tarjeta);
            loadTarjetas();
        });
    }

    public void updateTarjeta(Tarjeta tarjeta) {
        executorService.execute(() -> {
            db.tarjetaDao().update(tarjeta);
            loadTarjetas();
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
