package com.example.gestiondecompras.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gestiondecompras.R;
import com.example.gestiondecompras.adapters.PedidosAdapter;
import com.example.gestiondecompras.database.DatabaseHelper;
import com.example.gestiondecompras.databinding.ActivityReportesBinding;
import com.example.gestiondecompras.models.Pedido;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportesActivity extends AppCompatActivity implements PedidosAdapter.OnPedidoClickListener {

    private ActivityReportesBinding binding;
    private ReportesViewModel viewModel;
    private PedidosAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ReportesViewModel.class);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setTitle(R.string.reportes_title);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        observeViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadReportes();
    }

    private void setupRecyclerView() {
        binding.rvReportePedidos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PedidosAdapter(this);
        binding.rvReportePedidos.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getTotalCobrado().observe(this, total -> {
            if (total != null) {
                binding.tvTotalCobrado.setText(formatoMoneda(total));
            }
        });

        viewModel.getTotalPendiente().observe(this, total -> {
            if (total != null) {
                binding.tvTotalPendiente.setText(formatoMoneda(total));
            }
        });

        viewModel.getVentasGeneradas().observe(this, total -> {
            if (total != null) {
                binding.tvTotalVentas.setText(formatoMoneda(total));
            }
        });

        viewModel.getGananciaProyectada().observe(this, total -> {
            if (total != null) {
                binding.tvTotalGanancia.setText(formatoMoneda(total));
            }
        });

        viewModel.getPedidos().observe(this, pedidos -> {
            if (pedidos != null) {
                adapter.actualizarLista(pedidos);
            }
        });
    }

    private String formatoMoneda(double valor) {
        return String.format(Locale.getDefault(), "RD$ %,.2f", valor);
    }

    @Override
    public void onPedidoClick(Pedido pedido) {
        Intent intent = new Intent(this, NuevoPedidoActivity.class);
        intent.putExtra("pedido_id", pedido.id);
        startActivity(intent);
    }

    @Override
    public void onPedidoLongClick(Pedido pedido) {
        onPedidoClick(pedido);
    }
}
