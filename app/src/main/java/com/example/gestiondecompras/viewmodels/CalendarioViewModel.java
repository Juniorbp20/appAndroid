package com.example.gestiondecompras.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Pedido;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalendarioViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executorService;

    private final MutableLiveData<List<Pedido>> pedidos = new MutableLiveData<>();
    private final MutableLiveData<List<Pedido>> todosLosPedidos = new MutableLiveData<>();

    public CalendarioViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Pedido>> getPedidos() {
        return pedidos;
    }

    public LiveData<List<Pedido>> getTodosLosPedidos() {
        return todosLosPedidos;
    }

    public void loadPedidos(long epoch, String busqueda) {
        executorService.execute(() -> {
            pedidos.postValue(db.pedidoDao().pedidosPorDiaYCliente(epoch, busqueda));
        });
    }

    public void loadAllPedidos() {
        executorService.execute(() -> {
            todosLosPedidos.postValue(db.pedidoDao().getPedidosFiltrados("", "", null));
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
