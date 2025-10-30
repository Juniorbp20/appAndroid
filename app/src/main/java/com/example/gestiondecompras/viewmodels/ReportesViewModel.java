package com.example.gestiondecompras.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Pedido;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportesViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executorService;

    private final MutableLiveData<Double> totalCobrado = new MutableLiveData<>();
    private final MutableLiveData<Double> totalPendiente = new MutableLiveData<>();
    private final MutableLiveData<Double> ventasGeneradas = new MutableLiveData<>();
    private final MutableLiveData<Double> gananciaProyectada = new MutableLiveData<>();
    private final MutableLiveData<List<Pedido>> pedidos = new MutableLiveData<>();

    public ReportesViewModel(@NonNull Application application) {
        super(application);
        db = Room.databaseBuilder(application, AppDatabase.class, "GestionCompras.db").build();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<Double> getTotalCobrado() {
        return totalCobrado;
    }

    public LiveData<Double> getTotalPendiente() {
        return totalPendiente;
    }

    public LiveData<Double> getVentasGeneradas() {
        return ventasGeneradas;
    }

    public LiveData<Double> getGananciaProyectada() {
        return gananciaProyectada;
    }

    public LiveData<List<Pedido>> getPedidos() {
        return pedidos;
    }

    public void loadReportes() {
        executorService.execute(() -> {
            totalCobrado.postValue(db.reporteDao().totalCobrado());
            totalPendiente.postValue(db.reporteDao().totalPendiente());
            ventasGeneradas.postValue(db.reporteDao().ventasGeneradas());
            gananciaProyectada.postValue(db.reporteDao().gananciaProyectada());
            pedidos.postValue(db.pedidoDao().findByEstado("")); // Load all pedidos
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
