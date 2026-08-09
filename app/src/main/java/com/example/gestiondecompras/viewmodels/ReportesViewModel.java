package com.example.gestiondecompras.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Pedido;

import java.util.Collections;
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
    private final MutableLiveData<List<Pedido>> pedidosPagados = new MutableLiveData<>();

    public ReportesViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
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

    public LiveData<List<Pedido>> getPedidosPagados() {
        return pedidosPagados;
    }

    public void loadReportes(Long desde, Long hasta, String busqueda) {
        executorService.execute(() -> {
            Double cobrado = db.reporteDao().totalCobrado(desde, hasta, busqueda);
            Double pendiente = db.reporteDao().totalPendiente(desde, hasta, busqueda);
            Double ventas = db.reporteDao().ventasGeneradas(desde, hasta, busqueda);
            Double ganancia = db.reporteDao().gananciaProyectada(desde, hasta, busqueda);
            List<Pedido> porCobrar = db.pedidoDao().getPedidosPorCobrar(desde, hasta, busqueda);
            List<Pedido> pagados = db.pedidoDao().getPedidosPagadosFiltrados(desde, hasta, busqueda);

            totalCobrado.postValue(cobrado != null ? cobrado : 0d);
            totalPendiente.postValue(pendiente != null ? pendiente : 0d);
            ventasGeneradas.postValue(ventas != null ? ventas : 0d);
            gananciaProyectada.postValue(ganancia != null ? ganancia : 0d);
            pedidos.postValue(porCobrar != null ? porCobrar : Collections.emptyList());
            pedidosPagados.postValue(pagados != null ? pagados : Collections.emptyList());
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
