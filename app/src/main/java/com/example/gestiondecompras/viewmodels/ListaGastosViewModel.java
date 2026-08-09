package com.example.gestiondecompras.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Gasto;
import com.example.gestiondecompras.models.GastoCategoria;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListaGastosViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executorService;

    private final MutableLiveData<List<Gasto>> gastos = new MutableLiveData<>();
    private final MutableLiveData<Double> totalPeriodo = new MutableLiveData<>();
    private final MutableLiveData<Double> totalMes = new MutableLiveData<>();
    private final MutableLiveData<List<GastoCategoria>> totalesPorCategoria = new MutableLiveData<>();

    public ListaGastosViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Gasto>> getGastos() {
        return gastos;
    }

    public LiveData<Double> getTotalPeriodo() {
        return totalPeriodo;
    }

    public LiveData<Double> getTotalMes() {
        return totalMes;
    }

    public LiveData<List<GastoCategoria>> getTotalesPorCategoria() {
        return totalesPorCategoria;
    }

    public void loadGastos(Long desde, Long hasta, String busqueda) {
        executorService.execute(() -> {
            List<Gasto> lista = db.gastoDao().getGastosFiltrados(desde, hasta, busqueda);
            gastos.postValue(lista != null ? lista : Collections.emptyList());

            if (desde != null && hasta != null) {
                totalPeriodo.postValue(db.gastoDao().getTotalRango(desde, hasta));
                List<GastoCategoria> categorias = db.gastoDao().getTotalesPorCategoria(desde, hasta);
                totalesPorCategoria.postValue(categorias != null ? categorias : Collections.emptyList());
            }

            long[] mes = inicioYFinDelMes();
            totalMes.postValue(db.gastoDao().getTotalRango(mes[0], mes[1]));
        });
    }

    public void deleteGasto(Gasto gasto) {
        executorService.execute(() -> {
            db.gastoDao().delete(gasto);
            loadGastos(null, null, "");
        });
    }

    public static long[] inicioYFinDelMes() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long inicio = cal.getTimeInMillis();

        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.MILLISECOND, -1);
        long fin = cal.getTimeInMillis();
        return new long[]{inicio, fin};
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
