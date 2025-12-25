package com.example.gestiondecompras.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gestiondecompras.R;
import com.example.gestiondecompras.adapters.PedidosAdapter;
import com.example.gestiondecompras.databinding.ActivityCalendarioBinding;
import com.example.gestiondecompras.viewmodels.CalendarioViewModel;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarioActivity extends AppCompatActivity implements PedidosAdapter.OnPedidoClickListener {

    private ActivityCalendarioBinding binding;
    private CalendarioViewModel viewModel;
    private PedidosAdapter adapter;
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCalendarioBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(CalendarioViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupCalendar();
        setupRecyclerView();
        observeViewModel();

        binding.tvFechaSeleccionada.setText("Hoy: " + df.format(new Date()));
        viewModel.loadPedidos(new Date().getTime());
    }

    private void setupCalendar() {
        binding.calendarView.setOnDateChangeListener((v, y, m, d) -> {
            Calendar c = Calendar.getInstance();
            c.set(y, m, d, 0, 0, 0);
            Date sel = c.getTime();
            binding.tvFechaSeleccionada.setText(df.format(sel));
            viewModel.loadPedidos(sel.getTime());
        });
    }

    private void setupRecyclerView() {
        binding.rvPedidosDelDia.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PedidosAdapter(this);
        binding.rvPedidosDelDia.setAdapter(adapter);
    }

    @SuppressLint("SetTextI18n")
    private void observeViewModel() {
        viewModel.getPedidos().observe(this, pedidos -> {
            if (pedidos != null) {
                adapter.actualizarLista(pedidos);
                binding.tvCantidadPedidos.setText("(" + pedidos.size() + " pedidos)");
            } else {
                adapter.actualizarLista(Collections.emptyList());
                binding.tvCantidadPedidos.setText("(0 pedidos)");
            }
        });
    }

    @Override
    public void onPedidoClick(com.example.gestiondecompras.models.Pedido pedido) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_detalle_pedido, null);
        
        android.widget.TextView tvProducto = view.findViewById(R.id.tv_detalle_producto);
        android.widget.TextView tvTienda = view.findViewById(R.id.tv_detalle_tienda);
        android.widget.TextView tvTracking = view.findViewById(R.id.tv_detalle_tracking);
        android.widget.TextView tvFechaCompra = view.findViewById(R.id.tv_detalle_fecha_compra);
        android.widget.TextView tvFechaEntrega = view.findViewById(R.id.tv_detalle_fecha_entrega);
        android.widget.TextView tvCosto = view.findViewById(R.id.tv_detalle_costo);
        android.widget.TextView tvVenta = view.findViewById(R.id.tv_detalle_venta);
        android.widget.TextView tvGanancia = view.findViewById(R.id.tv_detalle_ganancia);
        android.widget.TextView tvEstado = view.findViewById(R.id.tv_detalle_estado);
        
        tvProducto.setText("Cliente: " + pedido.getClienteNombre());
        tvTienda.setText(pedido.getTienda());
        tvTracking.setText("N/A");
        
        tvFechaCompra.setText(pedido.getFechaRegistro() != null ? df.format(pedido.getFechaRegistro()) : "N/A");
        tvFechaEntrega.setText(pedido.getFechaEntrega() != null ? df.format(pedido.getFechaEntrega()) : "N/A");
        
        tvCosto.setText(String.format(Locale.getDefault(), "RD$ %,.2f", pedido.getMontoCompra()));
        tvVenta.setText(String.format(Locale.getDefault(), "RD$ %,.2f", pedido.getTotalGeneral()));
        tvGanancia.setText(String.format(Locale.getDefault(), "RD$ %,.2f", pedido.getGanancia()));
        
        tvEstado.setText(pedido.getEstado());
        
        int colorRes;
        if (com.example.gestiondecompras.models.Pedido.ESTADO_PAGADO.equalsIgnoreCase(pedido.getEstado())) {
            colorRes = R.color.status_paid;
        } else if (com.example.gestiondecompras.models.Pedido.ESTADO_ENTREGADO.equalsIgnoreCase(pedido.getEstado())) {
            colorRes = R.color.status_delivered;
        } else if (com.example.gestiondecompras.models.Pedido.ESTADO_CANCELADO.equalsIgnoreCase(pedido.getEstado())) {
            colorRes = R.color.status_cancelled;
        } else {
            colorRes = R.color.status_pending;
        }
        
        tvEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
            androidx.core.content.ContextCompat.getColor(this, colorRes)
        ));
        tvEstado.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.white));
        
        builder.setView(view)
               .setPositiveButton("Cerrar", null)
               .show();
    }

    @Override
    public void onPedidoLongClick(com.example.gestiondecompras.models.Pedido pedido) {
        // No action required or show simple toast
    }
}

