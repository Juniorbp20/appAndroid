package com.example.gestiondecompras.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

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
        db = Room.databaseBuilder(application, AppDatabase.class, "GestionCompras.db").build();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Cliente>> getClientes() {
        return clientes;
    }

    public void loadClientes() {
        executorService.execute(() -> {
            clientes.postValue(db.clienteDao().getAllClientes());
        });
    }

    public void deleteCliente(Cliente cliente) {
        executorService.execute(() -> {
            db.clienteDao().delete(cliente);
            loadClientes();
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
