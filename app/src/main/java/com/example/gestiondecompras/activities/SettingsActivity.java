package com.example.gestiondecompras.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.gestiondecompras.R;
import com.example.gestiondecompras.databinding.ActivitySettingsBinding;
import com.example.gestiondecompras.utils.GoogleDriveService;
import com.example.gestiondecompras.database.AppDatabase;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.services.drive.DriveScopes;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SharedPreferences sharedPreferences;
    private int selectedHour = 9;
    private int selectedMinute = 0;
    private GoogleSignInClient googleSignInClient;
    private GoogleDriveService googleDriveService;
    private Runnable pendingAction;
    private static final String DB_NAME = "GestionCompras.db";

    private final ActivityResultLauncher<android.content.Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    handleSignInResult(result.getData());
                } else {
                    Toast.makeText(this, "Inicio de sesión cancelado o fallido", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("settings", MODE_PRIVATE);

        setupGoogleSignIn();
        setupToolbar();
        loadSettings();
        setupListeners();
        updateGoogleLoginStatus();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadSettings() {
        int theme = sharedPreferences.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (theme == AppCompatDelegate.MODE_NIGHT_YES) binding.rbDark.setChecked(true);
        else if (theme == AppCompatDelegate.MODE_NIGHT_NO) binding.rbLight.setChecked(true);
        else binding.rbSystem.setChecked(true);

        int reminderDays = sharedPreferences.getInt("reminder_time", 3);
        binding.etReminderTime.setText(String.valueOf(reminderDays));

        selectedHour = sharedPreferences.getInt("reminder_hour", 9);
        selectedMinute = sharedPreferences.getInt("reminder_minute", 0);
        updateTimeText();

        boolean biometricEnabled = sharedPreferences.getBoolean("biometric_enabled", false);
        binding.swBiometric.setChecked(biometricEnabled);

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
        binding.tvReminderClockTime.setOnClickListener(v -> {
            new android.app.TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedHour = hourOfDay;
                selectedMinute = minute;
                updateTimeText();
            }, selectedHour, selectedMinute, true).show();
        });

        binding.btnReset.setOnClickListener(v -> showResetConfirmation());
        binding.btnContact.setOnClickListener(v -> contactSupport());
        binding.btnSaveSettings.setOnClickListener(v -> saveSettings());

        binding.btnDriveBackup.setOnClickListener(v -> {
            pendingAction = this::performDriveBackup;
            signInAndDo(pendingAction);
        });
        binding.btnDriveRestore.setOnClickListener(v -> {
            pendingAction = this::performDriveRestore;
            signInAndDo(pendingAction);
        });

        binding.btnGoogleLogout.setOnClickListener(v -> {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account != null) {
                logoutGoogle();
            } else {
                googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
            }
        });
    }

    private void signInAndDo(Runnable action) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null && account.getGrantedScopes().contains(new Scope(DriveScopes.DRIVE_FILE))) {
            googleDriveService = new GoogleDriveService(this, account.getEmail());
            action.run();
        } else {
            googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
        }
    }

    private void handleSignInResult(android.content.Intent data) {
        Task<GoogleSignInAccount> completedTask = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            googleDriveService = new GoogleDriveService(this, account.getEmail());
            updateGoogleLoginStatus();
            if (pendingAction != null) {
                pendingAction.run();
                pendingAction = null;
            }
        } catch (ApiException e) {
            Toast.makeText(this, "Error de Google: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
        }
    }

    private void logoutGoogle() {
        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            googleDriveService = null;
            updateGoogleLoginStatus();
            Toast.makeText(SettingsActivity.this, "Sesión de Google cerrada", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateGoogleLoginStatus() {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            binding.btnGoogleLogout.setText("Cerrar sesión de Google");
            binding.btnGoogleLogout.setTextColor(getResources().getColor(android.R.color.holo_red_dark, getTheme()));
        } else {
            binding.btnGoogleLogout.setText("Iniciar sesión");
            binding.btnGoogleLogout.setTextColor(getResources().getColor(android.R.color.holo_blue_dark, getTheme()));
        }
    }

    private void ensureDbFileExists() {
        AppDatabase db = AppDatabase.getInstance(this);
        db.getOpenHelper().getWritableDatabase();
    }

    private void performDriveBackup() {
        try {
            ensureDbFileExists();
            AppDatabase.getInstance(this).getOpenHelper().getWritableDatabase().execSQL("PRAGMA checkpoint(FULL)");
            
            File dbFile = getDatabasePath(DB_NAME);
            
            if (!dbFile.exists()) {
                Toast.makeText(this, "Error: El archivo de datos no existe", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Toast.makeText(this, "Subiendo a Drive...", Toast.LENGTH_SHORT).show();
            googleDriveService.uploadBackup(dbFile, new GoogleDriveService.DriveCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    runOnUiThread(() -> Toast.makeText(SettingsActivity.this, "Copia subida con éxito", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onError(Exception e) {
                    runOnUiThread(() -> Toast.makeText(SettingsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error al preparar respaldo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void performDriveRestore() {
        AppDatabase.getInstance(this).close();
        
        File dbFile = getDatabasePath(DB_NAME);
        File walFile = new File(dbFile.getPath() + "-wal");
        File shmFile = new File(dbFile.getPath() + "-shm");
        
        if (walFile.exists()) walFile.delete();
        if (shmFile.exists()) shmFile.delete();

        Toast.makeText(this, "Descargando de Drive...", Toast.LENGTH_SHORT).show();
        googleDriveService.downloadBackup(dbFile, new GoogleDriveService.DriveCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    Toast.makeText(SettingsActivity.this, "Restauración exitosa. Reiniciando...", Toast.LENGTH_LONG).show();
                    restartApp();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> Toast.makeText(SettingsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void saveSettings() {
        int theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        if (binding.rbDark.isChecked()) theme = AppCompatDelegate.MODE_NIGHT_YES;
        else if (binding.rbLight.isChecked()) theme = AppCompatDelegate.MODE_NIGHT_NO;
        
        AppCompatDelegate.setDefaultNightMode(theme);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("theme", theme);
        editor.putBoolean("biometric_enabled", binding.swBiometric.isChecked());
        editor.apply();

        Toast.makeText(this, "Configuración guardada", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void restartApp() {
        android.content.Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        finishAffinity();
    }

    private void showResetConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirmar reinicio")
            .setMessage("¿Estás seguro de que deseas borrar todos los datos?")
            .setPositiveButton("Borrar Todo", (dialog, which) -> {
                AppDatabase.getInstance(this).close();
                File dbFile = getDatabasePath(DB_NAME);
                new File(dbFile.getPath() + "-wal").delete();
                new File(dbFile.getPath() + "-shm").delete();
                if(dbFile.delete()) {
                    Toast.makeText(this, "Datos borrados", Toast.LENGTH_SHORT).show();
                    finishAffinity();
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void contactSupport() {
         android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SENDTO);
         intent.setData(android.net.Uri.parse("mailto:")); 
         intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"sssuport2@gmail.com"});
         intent.setPackage("com.google.android.gm");
         if (intent.resolveActivity(getPackageManager()) != null) {
             startActivity(intent);
         }
    }
}
