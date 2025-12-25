package com.example.gestiondecompras.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Cliente;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListaClientesViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executorService;

    private final MutableLiveData<List<Cliente>> clientes = new MutableLiveData<>();

    public ListaClientesViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Cliente>> getClientes() {
        return clientes;
    }

    public void loadClientes() {
        executorService.execute(() -> {
            // We cast validly because we change the return type logic or just feed it into the LiveData<List<Cliente>>
            // Since ClienteWithMetrics extends Cliente, we can post it to MutableLiveData<List<Cliente>>
            List<? extends Cliente> list = db.clienteDao().getAllClientesWithMetrics();
            clientes.postValue((List<Cliente>) list);
        });
    }

    public void deleteCliente(Cliente cliente) {
        executorService.execute(() -> {
            db.clienteDao().delete(cliente);
            loadClientes();
        });
    }

    public void insertCliente(Cliente cliente) {
        executorService.execute(() -> {
            db.clienteDao().insert(cliente);
            loadClientes();
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
