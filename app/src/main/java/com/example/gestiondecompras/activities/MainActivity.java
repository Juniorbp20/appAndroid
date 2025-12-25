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
        setupDrawer(); // NEW
        observeViewModel();
        
        checkBiometricSecurity();
        checkFirstRun();
    }

    private void checkFirstRun() {
        android.content.SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        boolean isFirstRun = prefs.getBoolean("is_first_run", true);
        if (isFirstRun) {
            startActivity(new Intent(this, AyudaActivity.class));
            prefs.edit().putBoolean("is_first_run", false).apply();
        }
    }

    private void checkBiometricSecurity() {
        android.content.SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean biometricEnabled = prefs.getBoolean("biometric_enabled", false);

        if (biometricEnabled) {
            java.util.concurrent.Executor executor = androidx.core.content.ContextCompat.getMainExecutor(this);
            androidx.biometric.BiometricPrompt biometricPrompt = new androidx.biometric.BiometricPrompt(MainActivity.this,
                    executor, new androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @androidx.annotation.NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    // If user canceled (pressed back), close app. 
                    // If no biometrics, we might want to let them in or show settings?
                    // For now, robustly close but show why.
                    if (errorCode == androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED || 
                        errorCode == androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        finishAffinity();
                    } else {
                         android.widget.Toast.makeText(MainActivity.this, "Error de autenticación: " + errString, android.widget.Toast.LENGTH_LONG).show();
                         finishAffinity();
                    }
                }

                @Override
                public void onAuthenticationSucceeded(@androidx.annotation.NonNull androidx.biometric.BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    // Continue to app
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    // Optional: Show toast
                }
            });

            // Allow PIN/Pattern as fallback if Biometric fails or is unavailable
            androidx.biometric.BiometricPrompt.PromptInfo promptInfo = new androidx.biometric.BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.biometric_title))
                    .setSubtitle(getString(R.string.biometric_subtitle))
                    .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG | 
                                              androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .build();

            biometricPrompt.authenticate(promptInfo);
        }
    }

    private void setupDrawer() {
        // Toolbar hamburger click opens drawer
        binding.toolbar.setNavigationOnClickListener(v -> 
            binding.drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
        );

        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (id == R.id.nav_nuevo_pedido) {
                startActivity(new Intent(this, NuevoPedidoActivity.class));
            } else if (id == R.id.nav_ver_pedidos) {
                startActivity(new Intent(this, ListaPedidosActivity.class));
            } else if (id == R.id.nav_calendario) {
                startActivity(new Intent(this, CalendarioActivity.class));
            } else if (id == R.id.nav_clientes) {
                startActivity(new Intent(this, ListaClientesActivity.class));
            } else if (id == R.id.nav_reportes) {
                startActivity(new Intent(this, ReportesActivity.class));
            } else if (id == R.id.nav_tarjetas) {
                startActivity(new Intent(this, TarjetasActivity.class));
            } else if (id == R.id.nav_ayuda) {
                startActivity(new Intent(this, AyudaActivity.class));
            }
            // Close drawer
            ((androidx.drawerlayout.widget.DrawerLayout) binding.getRoot()).closeDrawer(androidx.core.view.GravityCompat.START);
            return true;
        });
    }

    // Unlink old chip listeners (chips removed from XML)
    // Removed unused setupClickListeners logic for chips
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

        binding.cardTotalPendiente.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListaPedidosActivity.class);
            intent.putExtra("filtro_estado", "por_cobrar");
            startActivity(intent);
        });

        binding.cardTotalPagado.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListaPedidosActivity.class);
            intent.putExtra("filtro_estado", "pagado");
            startActivity(intent);
        });

        binding.cardPedidosAtrasados.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListaPedidosActivity.class);
            intent.putExtra("filtro_atrasados", true);
            startActivity(intent);
        });

        binding.cardGananciaEsperada.setOnClickListener(v -> showClientEarningsDialog());
    }

    @SuppressLint("DefaultLocale")
    private void observeViewModel() {
        viewModel.getDashboardData().observe(this, dashboardRow -> {
            if (dashboardRow != null) {
                binding.tvTotalPendiente.setText(String.format("RD$ %,.2f", dashboardRow.totalPendiente));
                binding.tvTotalPendienteCard.setText(String.format("RD$ %,.2f", dashboardRow.totalPendiente));
                binding.tvTotalPagado.setText(String.format("RD$ %,.2f", dashboardRow.totalPagado));
            }
        });

        viewModel.getActiveClientsCount().observe(this, count -> {
            // Mantener label estático en esta tarjeta.
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

    private void showClientEarningsDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        
        androidx.recyclerview.widget.RecyclerView rv = new androidx.recyclerview.widget.RecyclerView(this);
        rv.setLayoutManager(new LinearLayoutManager(this));
        com.example.gestiondecompras.adapters.ClienteGananciaAdapter adapter = new com.example.gestiondecompras.adapters.ClienteGananciaAdapter();
        rv.setAdapter(adapter);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        rv.setPadding(pad, pad, pad, pad);
        
        builder.setTitle("Ganancias por Cliente");
        builder.setView(rv);
        builder.setPositiveButton("Cerrar", null);
        
        viewModel.getClientEarnings().observe(this, adapter::actualizarLista);
        
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadDashboardData();
    }
}




