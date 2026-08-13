package com.example.gestiondecompras.activities;

import android.annotation.SuppressLint;
import android.util.Log;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gestiondecompras.R;
import com.example.gestiondecompras.adapters.PedidosAdapter;
import com.example.gestiondecompras.databinding.ActivityCalendarioBinding;
import com.example.gestiondecompras.models.Pedido;
import com.example.gestiondecompras.viewmodels.CalendarioViewModel;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import android.text.style.ForegroundColorSpan;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        setupSearch();
        observeViewModel();

        binding.tvFechaSeleccionada.setText("Hoy: " + df.format(new Date()));
        viewModel.loadPedidos(new Date().getTime(), "");
        viewModel.loadAllPedidos();
    }

    private void setupSearch() {
        binding.etBuscarCliente.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { reloadDay(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void reloadDay() {
        String busqueda = binding.etBuscarCliente.getText() != null
                ? binding.etBuscarCliente.getText().toString().trim() : "";
        CalendarDay selected = binding.calendarView.getSelectedDate();
        if (selected != null) {
            viewModel.loadPedidos(selected.getDate().getTime(), busqueda);
        }
    }

    private void setupCalendar() {
        binding.calendarView.setSelectedDate(new Date());
        binding.calendarView.setOnDateChangedListener((widget, date, selected) -> {
            binding.tvFechaSeleccionada.setText(df.format(date.getDate()));
            viewModel.loadPedidos(date.getDate().getTime(), binding.etBuscarCliente.getText() != null
                    ? binding.etBuscarCliente.getText().toString().trim() : "");
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
        viewModel.getTodosLosPedidos().observe(this, pedidos -> {
            if (pedidos != null) {
                aplicarColoresDias(pedidos);
            }
        });
    }

    private void aplicarColoresDias(List<Pedido> pedidos) {
        Map<String, Integer> coloresPorDia = new HashMap<>();
        for (Pedido p : pedidos) {
            if (p.fechaRegistroEpoch == null) continue;
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(p.fechaRegistroEpoch);
            String dia = c.get(Calendar.YEAR) + "-" + c.get(Calendar.MONTH) + "-" + c.get(Calendar.DAY_OF_MONTH);
            int color = colorDeEstado(p.getEstado());
            Integer actual = coloresPorDia.get(dia);
            if (actual == null || prioridadDe(color) < prioridadDe(actual)) {
                coloresPorDia.put(dia, color);
            }
        }
        binding.calendarView.removeDecorators();
        for (Map.Entry<String, Integer> e : coloresPorDia.entrySet()) {
            String[] partes = e.getKey().split("-");
            int year = Integer.parseInt(partes[0]);
            int month = Integer.parseInt(partes[1]);
            int day = Integer.parseInt(partes[2]);
            binding.calendarView.addDecorator(new ColorDiaDecorator(CalendarDay.from(year, month, day),
                    ContextCompat.getColor(this, e.getValue())));
        }
        binding.calendarView.invalidateDecorators();
    }

    private int colorDeEstado(String estado) {
        if (estado == null) return R.color.status_pending;
        if (Pedido.ESTADO_PAGADO.equalsIgnoreCase(estado)) return R.color.status_paid;
        if (Pedido.ESTADO_ENTREGADO.equalsIgnoreCase(estado)) return R.color.status_delivered;
        if (Pedido.ESTADO_CANCELADO.equalsIgnoreCase(estado)) return R.color.status_cancelled;
        return R.color.status_pending;
    }

    private int prioridadDe(int colorRes) {
        if (colorRes == R.color.status_pending) return 0;
        if (colorRes == R.color.status_delivered) return 1;
        if (colorRes == R.color.status_paid) return 2;
        return 3;
    }

    private static class ColorDiaDecorator implements DayViewDecorator {
        private final CalendarDay dia;
        private final int color;

        ColorDiaDecorator(CalendarDay dia, int color) {
            this.dia = dia;
            this.color = color;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return day.equals(dia);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(color));
        }
    }

    @Override
    public void onPedidoClick(com.example.gestiondecompras.models.Pedido pedido) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_detalle_pedido, null);
        
        android.widget.TextView tvProducto = view.findViewById(R.id.tv_detalle_producto);
        android.widget.TextView tvTienda = view.findViewById(R.id.tv_detalle_tienda);
        android.widget.TextView tvFechaCompra = view.findViewById(R.id.tv_detalle_fecha_compra);
        android.widget.TextView tvFechaEntrega = view.findViewById(R.id.tv_detalle_fecha_entrega);
        android.widget.TextView tvCosto = view.findViewById(R.id.tv_detalle_costo);
        android.widget.TextView tvVenta = view.findViewById(R.id.tv_detalle_venta);
        android.widget.TextView tvGanancia = view.findViewById(R.id.tv_detalle_ganancia);
        android.widget.TextView tvEstado = view.findViewById(R.id.tv_detalle_estado);
        com.google.android.material.button.MaterialButton btnEnviar = view.findViewById(R.id.btn_enviar_resumen);
        
        tvProducto.setText(pedido.getClienteNombre());
        tvTienda.setText(pedido.getTienda());
        
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
        
        btnEnviar.setOnClickListener(v -> enviarResumenCliente(pedido));

        builder.setView(view)
               .setPositiveButton("Cerrar", null)
               .show();
    }

    private void enviarResumenCliente(com.example.gestiondecompras.models.Pedido pedido) {
        String fechaRegistro = pedido.getFechaRegistro() != null ? df.format(pedido.getFechaRegistro()) : "N/A";
        String fechaEntrega = pedido.getFechaEntrega() != null ? df.format(pedido.getFechaEntrega()) : "N/A";

        String mensaje = "*Resumen del Pedido*\n\n"
                + getString(R.string.pedido_detalle_cliente) + ": " + pedido.getClienteNombre() + "\n"
                + getString(R.string.pedido_detalle_tienda) + ": " + pedido.getTienda() + "\n"
                + getString(R.string.pedido_detalle_fecha_compra) + ": " + fechaRegistro + "\n"
                + getString(R.string.pedido_detalle_fecha_entrega) + ": " + fechaEntrega + "\n"
                + getString(R.string.pedido_detalle_venta) + ": RD$ " + String.format(Locale.getDefault(), "%,.2f", pedido.getTotalGeneral()) + "\n"
                + "Estado: " + pedido.getEstado();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.pedido_share_subject, pedido.getClienteNombre()));
        intent.putExtra(Intent.EXTRA_TEXT, mensaje);
        startActivity(Intent.createChooser(intent, getString(R.string.pedido_enviar_resumen)));
    }

    @Override
    public void onPedidoLongClick(com.example.gestiondecompras.models.Pedido pedido) {
        // No action required or show simple toast
    }
}

