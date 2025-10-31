package com.example.gestiondecompras.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Pedido;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListaPedidosViewModel extends AndroidViewModel {

    private final AppDatabase db;
    private final ExecutorService executorService;

    private final MutableLiveData<List<Pedido>> pedidos = new MutableLiveData<>();

    public ListaPedidosViewModel(@NonNull Application application) {
        super(application);
        db = AppDatabase.getInstance(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Pedido>> getPedidos() {
        return pedidos;
    }

    public void loadPedidos(String estado, String busqueda, Integer clienteId) {
        executorService.execute(() -> {
            String trimmedSearch = busqueda == null ? "" : busqueda.trim();
            String estadoQuery = mapEstado(estado);
            List<Pedido> resultado;

            if (isAtrasados(estado)) {
                resultado = db.pedidoDao().getPedidosAtrasados(System.currentTimeMillis());
                resultado = filtrar(resultado, trimmedSearch, clienteId);
            } else {
                resultado = db.pedidoDao().getPedidosFiltrados(estadoQuery, trimmedSearch, clienteId);
            }

            pedidos.postValue(resultado);
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

    private boolean isAtrasados(String estado) {
        return estado != null && estado.equalsIgnoreCase("Atrasados");
    }

    private String mapEstado(String estado) {
        if (estado == null) return "";
        switch (estado.toLowerCase(Locale.getDefault())) {
            case "pendientes":
                return Pedido.ESTADO_PENDIENTE;
            case "entregados":
                return Pedido.ESTADO_ENTREGADO;
            case "pagados":
                return Pedido.ESTADO_PAGADO;
            case "cancelados":
                return Pedido.ESTADO_CANCELADO;
            default:
                return "";
        }
    }

    private List<Pedido> filtrar(List<Pedido> origen, String busqueda, Integer clienteId) {
        if ((busqueda == null || busqueda.isEmpty()) && clienteId == null) {
            return origen;
        }
        List<Pedido> filtrados = new ArrayList<>();
        String criterio = busqueda == null ? "" : busqueda.toLowerCase(Locale.getDefault());
        for (Pedido pedido : origen) {
            boolean coincideCliente = clienteId == null || pedido.getClienteId() == clienteId;
            boolean coincideBusqueda = criterio.isEmpty()
                    || pedido.getClienteNombre().toLowerCase(Locale.getDefault()).contains(criterio)
                    || pedido.getTienda().toLowerCase(Locale.getDefault()).contains(criterio);
            if (coincideCliente && coincideBusqueda) {
                filtrados.add(pedido);
            }
        }
        return filtrados;
    }
}
