package com.example.gestiondecompras.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Cliente;
import com.example.gestiondecompras.models.Pedido;
import com.example.gestiondecompras.models.Tarjeta;
import com.example.gestiondecompras.models.Tienda;

import java.util.List;

public class NuevoPedidoViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executorService;

    private final MutableLiveData<List<Cliente>> clientes = new MutableLiveData<>();
    private final MutableLiveData<List<Tienda>> tiendas = new MutableLiveData<>();
    private final MutableLiveData<List<Tarjeta>> tarjetas = new MutableLiveData<>();

    public NuevoPedidoViewModel(@NonNull Application application) {
        super(application);
        db = Room.databaseBuilder(application, AppDatabase.class, "GestionCompras.db").build();
        executorService = Executors.newSingleThreadExecutor();
        loadInitialData();
    }

    public LiveData<List<Cliente>> getClientes() {
        return clientes;
    }

    public LiveData<List<Tienda>> getTiendas() {
        return tiendas;
    }

    public LiveData<List<Tarjeta>> getTarjetas() {
        return tarjetas;
    }

    public void loadInitialData() {
        executorService.execute(() -> {
            clientes.postValue(db.clienteDao().getAllClientes());
            tiendas.postValue(db.tiendaDao().getAllTiendas());
            tarjetas.postValue(db.tarjetaDao().getAllTarjetas());
        });
    }

    public void savePedido(Pedido pedido, Tarjeta tarjeta) {
        executorService.execute(() -> {
            long pedidoId = db.pedidoDao().insert(pedido);
            pedido.id = pedidoId;

            tarjeta.deudaActual += pedido.montoCompra;
            db.tarjetaDao().update(tarjeta);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
