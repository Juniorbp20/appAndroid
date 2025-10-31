package com.example.gestiondecompras.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gestiondecompras.R;
import com.example.gestiondecompras.adapters.PedidosAdapter;
import com.example.gestiondecompras.databinding.ActivityMainBinding;
import com.example.gestiondecompras.models.Pedido;
import com.example.gestiondecompras.viewmodels.DashboardViewModel;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DashboardViewModel viewModel;
    private PedidosAdapter proximosAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        updateGreeting();
        setupUpcomingList();
        setupClickListeners();
        observeViewModel();
    }

    private void updateGreeting() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int greetingRes;
        if (hour < 12) {
            greetingRes = R.string.dashboard_greeting_morning;
        } else if (hour < 18) {
            greetingRes = R.string.dashboard_greeting_afternoon;
        } else {
            greetingRes = R.string.dashboard_greeting_evening;
        }
        binding.tvGreeting.setText(getString(greetingRes));
    }

    private void setupUpcomingList() {
        proximosAdapter = new PedidosAdapter(null);
        binding.rvProximosPedidos.setLayoutManager(new LinearLayoutManager(this));
        binding.rvProximosPedidos.setAdapter(proximosAdapter);
        binding.rvProximosPedidos.setClickable(false);
        binding.rvProximosPedidos.setFocusable(false);
    }

    private void setupClickListeners() {
        binding.chipNuevoPedido.setOnClickListener(v ->
                startActivity(new Intent(this, NuevoPedidoActivity.class)));

        binding.chipListaPedidos.setOnClickListener(v ->
                startActivity(new Intent(this, ListaPedidosActivity.class)));

        binding.chipCalendario.setOnClickListener(v ->
                startActivity(new Intent(this, CalendarioActivity.class)));

        binding.chipClientes.setOnClickListener(v ->
                startActivity(new Intent(this, ListaClientesActivity.class)));

        binding.chipReportes.setOnClickListener(v ->
                startActivity(new Intent(this, ReportesActivity.class)));

        binding.chipTarjetas.setOnClickListener(v ->
                startActivity(new Intent(this, TarjetasActivity.class)));
    }

    @SuppressLint("DefaultLocale")
    private void observeViewModel() {
        viewModel.getDashboardData().observe(this, dashboardRow -> {
            if (dashboardRow != null) {
                binding.tvTotalPendiente.setText(String.format("RD$ %,.2f", dashboardRow.totalPendiente));
            }
        });

        viewModel.getActiveClientsCount().observe(this, count -> {
            // Mantener label est�tico en esta tarjeta.
        });

        viewModel.getOverdueOrdersCount().observe(this, count -> {
            if (count != null) {
                binding.tvPedidosAtrasados.setText(String.valueOf(count));
            }
        });

        viewModel.getProjectedEarnings().observe(this, earnings -> {
            if (earnings != null) {
                binding.tvGananciaEsperadaCard.setText(String.format("RD$ %,.2f", earnings));
            }
        });

        viewModel.getUpcomingOrders().observe(this, pedidos -> {
            if (pedidos != null && !pedidos.isEmpty()) {
                binding.emptyState.setVisibility(View.GONE);
                binding.rvProximosPedidos.setVisibility(View.VISIBLE);
                proximosAdapter.actualizarLista(pedidos);
            } else {
                binding.rvProximosPedidos.setVisibility(View.GONE);
                binding.emptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadDashboardData();
    }
}




