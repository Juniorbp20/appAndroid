package com.example.gestiondecompras.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Cliente;
import com.example.gestiondecompras.models.Pedido;
import com.example.gestiondecompras.models.Tarjeta;
import com.example.gestiondecompras.models.Tienda;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NuevoPedidoViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executorService;
    private static final List<String> DEFAULT_TIENDAS = Arrays.asList(
            "Temu",
            "Shein",
            "Amazon",
            "Ebay",
            "AliExpress",
            "Alibaba"
    );

    private final MutableLiveData<List<Cliente>> clientes = new MutableLiveData<>();
    private final MutableLiveData<List<Tienda>> tiendas = new MutableLiveData<>();
    private final MutableLiveData<List<Tarjeta>> tarjetas = new MutableLiveData<>();

    public NuevoPedidoViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
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

            List<Tienda> tiendasActuales = new ArrayList<>(db.tiendaDao().getAllTiendas());
            if (tiendasActuales.isEmpty()) {
                for (String nombre : DEFAULT_TIENDAS) {
                    Tienda tienda = new Tienda();
                    tienda.setNombre(nombre);
                    db.tiendaDao().insert(tienda);
                }
                tiendasActuales = db.tiendaDao().getAllTiendas();
            }
            tiendas.postValue(tiendasActuales);

            tarjetas.postValue(db.tarjetaDao().getAllTarjetas());
        });
    }

    public void savePedido(Pedido pedido, Tarjeta tarjeta) {
        executorService.execute(() -> {
            if (pedido.getId() > 0) {
                // Determine valid logic for updates. For now, we update the entry.
                // NOTE: Debt adjustment logic on edit is complex (need old value). 
                // We will assume for this step we just update the order details.
                db.pedidoDao().update(pedido);
            } else {
                long pedidoId = db.pedidoDao().insert(pedido);
                pedido.id = pedidoId;
                
                // Only increase debt on NEW orders for now to avoid double counting on edits without tracking diffs
                if (tarjeta != null) {
                    tarjeta.setDeudaActual(tarjeta.getDeudaActual() + pedido.getMontoCompra());
                    db.tarjetaDao().update(tarjeta);
                }
            }
        });
    }
    
    public LiveData<Pedido> getPedido(long id) {
        MutableLiveData<Pedido> pedidoLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Pedido p = db.pedidoDao().getPedidoById(id);
            pedidoLiveData.postValue(p);
        });
        return pedidoLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
