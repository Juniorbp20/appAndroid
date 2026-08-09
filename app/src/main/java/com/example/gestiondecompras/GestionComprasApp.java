package com.example.gestiondecompras;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.gestiondecompras.workers.NotificationWorker;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.color.DynamicColors;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class GestionComprasApp extends Application {

    private static final String TRABAJO_RECORDATORIOS = "recordatorios_diarios";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Restaurar el tema guardado (claro/oscuro/sistema) antes de crear cualquier Activity
        int theme = getSharedPreferences("settings", MODE_PRIVATE)
                .getInt("theme", androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(theme);
        
        // DESACTIVAR COLORES DINÁMICOS (Evita el morado de Android 12+)
        DynamicColors.isDynamicColorAvailable();// No aplicamos DynamicColors.applyToActivitiesIfAvailable(this);
// Si ya estaba puesto por el asistente de Android Studio, esta es la causa.

        createNotificationChannels();
        scheduleDailyReminders();
        
        // Inicializar el SDK de anuncios de Google
        new Thread(() -> MobileAds.initialize(this, initializationStatus -> {})).start();
    }

    private void createNotificationChannels() {
        CharSequence name = "Default Channel";
        String description = "Channel for default notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("default_channel", name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        NotificationWorker.crearCanales(this);
    }

    private void scheduleDailyReminders() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        int hora = prefs.getInt("reminder_hour", 9);
        int minuto = prefs.getInt("reminder_minute", 0);

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(NotificationWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(milisegundosHasta(hora, minuto), TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                TRABAJO_RECORDATORIOS,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest);
    }

    private long milisegundosHasta(int hora, int minuto) {
        Calendar now = Calendar.getInstance();
        Calendar next = (Calendar) now.clone();
        next.set(Calendar.HOUR_OF_DAY, hora);
        next.set(Calendar.MINUTE, minuto);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.before(now)) {
            next.add(Calendar.DAY_OF_YEAR, 1);
        }
        return next.getTimeInMillis() - now.getTimeInMillis();
    }
}