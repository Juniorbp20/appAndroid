package com.example.gestiondecompras.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Tarjeta;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListaTarjetasViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executorService;

    private final MutableLiveData<List<Tarjeta>> tarjetas = new MutableLiveData<>();
    private final MutableLiveData<String> mensajes = new MutableLiveData<>();

    public ListaTarjetasViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Tarjeta>> getTarjetas() {
        return tarjetas;
    }

    public LiveData<String> getMensajes() {
        return mensajes;
    }

    public void loadTarjetas() {
        executorService.execute(() -> tarjetas.postValue(db.tarjetaDao().getAllTarjetas()));
    }

    public void deleteTarjeta(Tarjeta tarjeta) {
        executorService.execute(() -> {
            int enlaces = db.pedidoDao().countPedidosPorTarjeta(tarjeta.id);
            if (enlaces > 0) {
                mensajes.postValue("tarjeta_en_uso");
            } else {
                db.tarjetaDao().delete(tarjeta);
                mensajes.postValue("tarjeta_eliminada");
                loadTarjetas();
            }
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
