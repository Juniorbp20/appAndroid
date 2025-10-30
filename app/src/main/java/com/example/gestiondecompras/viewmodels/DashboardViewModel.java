package com.example.gestiondecompras.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.DashboardRow;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executorService;

    private final MutableLiveData<DashboardRow> dashboardData = new MutableLiveData<>();
    private final MutableLiveData<Integer> activeClientsCount = new MutableLiveData<>();
    private final MutableLiveData<Integer> overdueOrdersCount = new MutableLiveData<>();
    private final MutableLiveData<Double> projectedEarnings = new MutableLiveData<>();

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        db = Room.databaseBuilder(application, AppDatabase.class, "GestionCompras.db").build();
        executorService = Executors.newSingleThreadExecutor();
        loadDashboardData();
    }

    public LiveData<DashboardRow> getDashboardData() {
        return dashboardData;
    }

    public LiveData<Integer> getActiveClientsCount() {
        return activeClientsCount;
    }

    public LiveData<Integer> getOverdueOrdersCount() {
        return overdueOrdersCount;
    }

    public void loadDashboardData() {
        executorService.execute(() -> {
            DashboardRow row = db.pedidoDao().getDashboard();
            dashboardData.postValue(row);

            int clients = db.clienteDao().getAllClientes().size();
            activeClientsCount.postValue(clients);

            int overdue = db.pedidoDao().getOverdueOrdersCount(System.currentTimeMillis());
            overdueOrdersCount.postValue(overdue);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    public LiveData<Object> getProjectedEarnings() {
        return null;
    }
}
