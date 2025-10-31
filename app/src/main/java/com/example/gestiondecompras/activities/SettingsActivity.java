package com.example.gestiondecompras.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.gestiondecompras.databinding.ActivitySettingsBinding;
import com.example.gestiondecompras.workers.NotificationWorker;

import java.util.concurrent.TimeUnit;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);

        setupToolbar();
        loadSettings();
        setupSaveButton();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadSettings() {
        int theme = sharedPreferences.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (theme == AppCompatDelegate.MODE_NIGHT_YES) {
            binding.rbDark.setChecked(true);
        } else if (theme == AppCompatDelegate.MODE_NIGHT_NO) {
            binding.rbLight.setChecked(true);
        } else {
            binding.rbSystem.setChecked(true);
        }

        int reminderTime = sharedPreferences.getInt("reminder_time", 3);
        binding.etReminderTime.setText(String.valueOf(reminderTime));
    }

    private void setupSaveButton() {
        binding.btnSaveSettings.setOnClickListener(v -> saveSettings());
    }

    private void saveSettings() {
        int theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        if (binding.rbDark.isChecked()) {
            theme = AppCompatDelegate.MODE_NIGHT_YES;
        } else if (binding.rbLight.isChecked()) {
            theme = AppCompatDelegate.MODE_NIGHT_NO;
        }
        AppCompatDelegate.setDefaultNightMode(theme);

        int reminderTime = Integer.parseInt(binding.etReminderTime.getText().toString());

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("theme", theme);
        editor.putInt("reminder_time", reminderTime);
        editor.apply();

        
        WorkManager.getInstance(this).cancelAllWork();
        
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(NotificationWorker.class, 1, TimeUnit.DAYS).build();
        WorkManager.getInstance(this).enqueue(workRequest);

        Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show();
        finish();
    }
}
