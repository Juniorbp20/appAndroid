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

public class ListaPedidosViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executorService;

    private final MutableLiveData<List<Pedido>> pedidos = new MutableLiveData<>();

    public ListaPedidosViewModel(@NonNull Application application) {
        super(application);
        db = Room.databaseBuilder(application, AppDatabase.class, "GestionCompras.db").build();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Pedido>> getPedidos() {
        return pedidos;
    }

    public void loadPedidos(String estado, String busqueda, Integer clienteId) {
        executorService.execute(() -> {
            pedidos.postValue(db.pedidoDao().getPedidosFiltrados(estado, busqueda, clienteId));
        });
    }

    public void deletePedido(Pedido pedido) {
        executorService.execute(() -> {
            db.pedidoDao().delete(pedido);
        });
    }

    public void updatePedido(Pedido pedido) {
        executorService.execute(() -> {
            db.pedidoDao().update(pedido);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
