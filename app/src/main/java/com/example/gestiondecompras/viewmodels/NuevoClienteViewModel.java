package com.example.gestiondecompras.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Cliente;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NuevoClienteViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executorService;

    public NuevoClienteViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void saveCliente(Cliente cliente) {
        executorService.execute(() -> {
            db.clienteDao().insert(cliente);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
