package com.example.gestiondecompras.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Gasto;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NuevoGastoViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executorService;

    public NuevoGastoViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void insertGasto(Gasto gasto, MutableLiveData<Boolean> resultado) {
        executorService.execute(() -> resultado.postValue(db.gastoDao().insert(gasto) > 0));
    }

    public void updateGasto(Gasto gasto, MutableLiveData<Boolean> resultado) {
        executorService.execute(() -> resultado.postValue(db.gastoDao().update(gasto) > 0));
    }

    public void getGasto(long id, MutableLiveData<Gasto> resultado) {
        executorService.execute(() -> {
            for (Gasto g : db.gastoDao().getAllGastos()) {
                if (g.getId() == id) {
                    resultado.postValue(g);
                    return;
                }
            }
            resultado.postValue(null);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
