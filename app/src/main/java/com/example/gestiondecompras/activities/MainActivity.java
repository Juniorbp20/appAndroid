package com.example.gestiondecompras.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.gestiondecompras.R;
import com.example.gestiondecompras.adapters.PedidosAdapter;
import com.example.gestiondecompras.databinding.ActivityMainBinding;
import com.example.gestiondecompras.utils.UpdateChecker;
import com.google.android.gms.ads.AdRequest;
import com.example.gestiondecompras.viewmodels.DashboardViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import java.io.File;
import java.util.Calendar;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DashboardViewModel viewModel;
    private PedidosAdapter proximosAdapter;
    private long downloadId = -1;
    private int intentosDescarga = 0;
    private final Handler actualizacionHandler = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Permiso de notificaciones concedido", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "No se podrán mostrar notificaciones de recordatorio", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applySystemBarsPadding();

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        updateGreeting();
        setupUpcomingList();
        setupClickListeners();
        setupDrawer(); 
        observeViewModel();
        
        checkBiometricSecurity();
        checkFirstRun();
        loadBannerAd();
        askNotificationPermission();
        updateGoogleUserInfo();
        checkForUpdates();
    }

    private void applySystemBarsPadding() {
        View contentRoot = (View) binding.toolbar.getParent();
        if (Build.VERSION.SDK_INT >= 30) {
            android.view.WindowInsets wi = getWindow().getDecorView().getRootWindowInsets();
            int top = 0, bottom = 0;
            if (wi != null) {
                top = wi.getInsets(android.view.WindowInsets.Type.statusBars()).top;
                bottom = wi.getInsets(android.view.WindowInsets.Type.navigationBars()).bottom;
            }
            contentRoot.setPadding(contentRoot.getPaddingLeft(), top,
                    contentRoot.getPaddingRight(), bottom);
        } else {
            int statusBarId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (statusBarId > 0) {
                int top = getResources().getDimensionPixelSize(statusBarId);
                contentRoot.setPadding(contentRoot.getPaddingLeft(), top,
                        contentRoot.getPaddingRight(), contentRoot.getPaddingBottom());
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) applySystemBarsPadding();
    }

    private void updateGoogleUserInfo() {
        View headerView = binding.navView.getHeaderView(0);
        if (headerView == null) return;

        ImageView ivProfile = headerView.findViewById(R.id.iv_google_profile);
        TextView tvName = headerView.findViewById(R.id.tv_google_name);
        TextView tvEmail = headerView.findViewById(R.id.tv_google_email);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            tvName.setText(account.getDisplayName());
            tvEmail.setText(account.getEmail());
            if (account.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(account.getPhotoUrl())
                        .placeholder(R.drawable.ic_shoppong)
                        .circleCrop()
                        .into(ivProfile);
            }
        } else {
            tvName.setText(getString(R.string.app_name));
            tvEmail.setText("Sin cuenta vinculada");
            ivProfile.setImageResource(R.drawable.ic_shoppong);
        }
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void loadBannerAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        binding.adView.loadAd(adRequest);
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
            Executor executor = ContextCompat.getMainExecutor(this);
            androidx.biometric.BiometricPrompt biometricPrompt = new androidx.biometric.BiometricPrompt(MainActivity.this,
                    executor, new androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    if (errorCode == androidx.biometric.BiometricPrompt.ERROR_USER_CANCELED || 
                        errorCode == androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        finishAffinity();
                    } else {
                         Toast.makeText(MainActivity.this, "Error de autenticación: " + errString, Toast.LENGTH_LONG).show();
                         finishAffinity();
                    }
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull androidx.biometric.BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                }
            });

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
        binding.navView.setCheckedItem(R.id.nav_home);

        binding.toolbar.setNavigationOnClickListener(v -> 
            binding.drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
        );

        ViewCompat.setOnApplyWindowInsetsListener(binding.navView, (v, insets) -> {
            Insets statusBar = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            View header = binding.navView.getHeaderView(0);
            if (header != null && statusBar.top > 0) {
                header.setPadding(header.getPaddingLeft(), statusBar.top,
                        header.getPaddingRight(), header.getPaddingBottom());
            }
            return insets;
        });

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
            } else if (id == R.id.nav_gastos) {
                startActivity(new Intent(this, ListaGastosActivity.class));
            } else if (id == R.id.nav_tarjetas) {
                startActivity(new Intent(this, TarjetasActivity.class));
            } else if (id == R.id.nav_ayuda) {
                startActivity(new Intent(this, AyudaActivity.class));
            }
            binding.drawerLayout.closeDrawer(androidx.core.view.GravityCompat.START);
            return true;
        });
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

        binding.cardGastosMes.setOnClickListener(v ->
                startActivity(new Intent(this, ListaGastosActivity.class)));
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

        viewModel.getMonthExpenses().observe(this, total -> {
            if (total != null) {
                binding.tvGastosMes.setText(String.format("RD$ %,.2f", total));
            }
        });
    }

    private void checkForUpdates() {
        UpdateChecker.check(new UpdateChecker.Callback() {
            @Override
            public void onUpdateAvailable(String version, String apkUrl) {
                mostrarDialogoActualizacion(version, apkUrl);
            }

            @Override
            public void onNoUpdate() {
            }
        });
    }

    private void mostrarDialogoActualizacion(String version, String apkUrl) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.actualizacion_disponible)
                .setMessage(getString(R.string.actualizacion_mensaje, version))
                .setPositiveButton(R.string.actualizar, (d, w) -> descargarApk(apkUrl))
                .setNegativeButton(R.string.ahora_no, null)
                .show();
    }

    private void descargarApk(String url) {
        try {
            File destino = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "gestion-compras.apk");
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(getString(R.string.descargando_actualizacion));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationUri(Uri.fromFile(destino));
            downloadId = ((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(request);
            Toast.makeText(this, R.string.descarga_iniciada, Toast.LENGTH_LONG).show();
            intentosDescarga = 0;
            esperarDescarga();
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_descarga, Toast.LENGTH_LONG).show();
        }
    }

    private void esperarDescarga() {
        actualizacionHandler.postDelayed(() -> {
            int status = -1;
            try {
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                try (android.database.Cursor cursor = dm.query(new DownloadManager.Query().setFilterById(downloadId))) {
                    if (cursor != null && cursor.moveToFirst()) {
                        status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                    }
                }
            } catch (Exception ignored) {
            }
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                instalarApk();
            } else if (status == DownloadManager.STATUS_FAILED) {
                Toast.makeText(this, R.string.error_instalacion, Toast.LENGTH_LONG).show();
            } else if (intentosDescarga++ < 100) {
                esperarDescarga();
            } else {
                Toast.makeText(this, R.string.error_instalacion, Toast.LENGTH_LONG).show();
            }
        }, 1500);
    }

    private void instalarApk() {
        File apk = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "gestion-compras.apk");
        if (!apk.exists()) {
            Toast.makeText(this, R.string.error_instalacion, Toast.LENGTH_LONG).show();
            return;
        }
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", apk);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_instalacion, Toast.LENGTH_LONG).show();
        }
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
    protected void onRestart() {
        super.onRestart();
        updateGoogleUserInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadDashboardData();
        if (binding.adView != null) {
            binding.adView.resume();
        }
        updateGoogleUserInfo();
    }

    @Override
    protected void onPause() {
        if (binding.adView != null) {
            binding.adView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (binding.adView != null) {
            binding.adView.destroy();
        }
        actualizacionHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
