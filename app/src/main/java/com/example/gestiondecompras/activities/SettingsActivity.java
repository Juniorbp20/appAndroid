package com.example.gestiondecompras.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.gestiondecompras.R;
import com.example.gestiondecompras.databinding.ActivitySettingsBinding;
import com.example.gestiondecompras.workers.NotificationWorker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SharedPreferences sharedPreferences;
    private int selectedHour = 9;
    private int selectedMinute = 0;

    // Launchers for SAF
    private final ActivityResultLauncher<String> createBackupLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/octet-stream"),
            uri -> {
                if (uri != null) {
                    try {
                        File dbFile = getDatabasePath("gestion_compras_db");
                        try (FileInputStream fis = new FileInputStream(dbFile);
                             OutputStream out = getContentResolver().openOutputStream(uri)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = fis.read(buffer)) > 0) {
                                out.write(buffer, 0, length);
                            }
                        }
                        Toast.makeText(this, "Copia de seguridad guardada exitosamente", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error al guardar copia: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private final ActivityResultLauncher<String[]> restoreBackupLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    try {
                        File dbFile = getDatabasePath("gestion_compras_db");
                        // Ensure DB directory exists
                        File dbDir = dbFile.getParentFile();
                        if (!dbDir.exists()) dbDir.mkdirs();

                        try (InputStream in = getContentResolver().openInputStream(uri);
                             FileOutputStream fos = new FileOutputStream(dbFile)) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = in.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
                            }
                        }
                        Toast.makeText(this, R.string.restore_success, Toast.LENGTH_SHORT).show();
                        restartApp();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error al restaurar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);

        setupToolbar();
        loadSettings();
        setupListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadSettings() {
        // Theme
        int theme = sharedPreferences.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (theme == AppCompatDelegate.MODE_NIGHT_YES) binding.rbDark.setChecked(true);
        else if (theme == AppCompatDelegate.MODE_NIGHT_NO) binding.rbLight.setChecked(true);
        else binding.rbSystem.setChecked(true);

        // Reminder Days
        int reminderDays = sharedPreferences.getInt("reminder_time", 3);
        binding.etReminderTime.setText(String.valueOf(reminderDays));

        // Reminder Time
        selectedHour = sharedPreferences.getInt("reminder_hour", 9);
        selectedMinute = sharedPreferences.getInt("reminder_minute", 0);
        updateTimeText();

        // Biometric
        boolean biometricEnabled = sharedPreferences.getBoolean("biometric_enabled", false);
        binding.swBiometric.setChecked(biometricEnabled);

        // Version
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            binding.tvVersion.setText("Versión " + versionName);
        } catch (Exception e) {
            binding.tvVersion.setText("Versión Desconocida");
        }
    }

    private void updateTimeText() {
        binding.tvReminderClockTime.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
    }

    private void setupListeners() {
        // Time Picker
        binding.tvReminderClockTime.setOnClickListener(v -> {
            new android.app.TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedHour = hourOfDay;
                selectedMinute = minute;
                updateTimeText();
            }, selectedHour, selectedMinute, true).show();
        });

        // Backup
        binding.btnBackup.setOnClickListener(v -> backupDatabase());

        // Restore
        binding.btnRestore.setOnClickListener(v -> restoreDatabase());

        // Reset
        binding.btnReset.setOnClickListener(v -> showResetConfirmation());

        // Contact
        binding.btnContact.setOnClickListener(v -> contactSupport());

        // Save
        binding.btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void saveSettings() {
        int theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        if (binding.rbDark.isChecked()) theme = AppCompatDelegate.MODE_NIGHT_YES;
        else if (binding.rbLight.isChecked()) theme = AppCompatDelegate.MODE_NIGHT_NO;
        
        AppCompatDelegate.setDefaultNightMode(theme);

        int reminderDays;
        try {
            reminderDays = Integer.parseInt(binding.etReminderTime.getText().toString());
        } catch (NumberFormatException e) {
            reminderDays = 3;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("theme", theme);
        editor.putInt("reminder_time", reminderDays);
        editor.putInt("reminder_hour", selectedHour);
        editor.putInt("reminder_minute", selectedMinute);
        editor.putBoolean("biometric_enabled", binding.swBiometric.isChecked());
        editor.apply();

        WorkManager.getInstance(this).cancelAllWork();
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(NotificationWorker.class, 1, TimeUnit.DAYS).build();
        WorkManager.getInstance(this).enqueue(workRequest);

        Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void backupDatabase() {
        String fileName = "backup_gestion_compras_" + 
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".db";
        createBackupLauncher.launch(fileName);
    }
    
    private void restoreDatabase() {
        restoreBackupLauncher.launch(new String[]{"*/*"}); // Accept any file type to be safe, or application/octet-stream
    }

    private void restartApp() {
        android.content.Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        finishAffinity(); // Kill current instance
    }

    private void showResetConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.reset_confirm_title)
            .setMessage(R.string.reset_confirm_message)
            .setPositiveButton("Borrar Todo", (dialog, which) -> {
                // Clear Data logic
                // Requires Database Access. Quick workaround: Delete DB file.
                File dbFile = getDatabasePath("gestion_compras_db");
                if(dbFile.delete()) {
                    Toast.makeText(this, "Datos borrados. Reiniciando...", Toast.LENGTH_SHORT).show();
                    finishAffinity(); // Close app
                } else {
                    Toast.makeText(this, "Error al borrar datos", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void contactSupport() {
         android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
         intent.setData(android.net.Uri.parse("mailto:")); 
         intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"soporte@ejemplo.com"});
         intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Sugerencia - Gestión de Compras");
         if (intent.resolveActivity(getPackageManager()) != null) {
             startActivity(intent);
         } else {
             Toast.makeText(this, "No hay app de correo instalada", Toast.LENGTH_SHORT).show();
         }
    }
}
