package com.example.gestiondecompras.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gestiondecompras.adapters.PedidosAdapter;
import com.example.gestiondecompras.databinding.ActivityCalendarioBinding;
import com.example.gestiondecompras.viewmodels.CalendarioViewModel;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalendarioActivity extends AppCompatActivity {

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
        adapter = new PedidosAdapter(null);
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
}

