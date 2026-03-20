package com.example.gestiondecompras;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.color.DynamicColors;

public class GestionComprasApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // DESACTIVAR COLORES DINÁMICOS (Evita el morado de Android 12+)
        if (DynamicColors.isDynamicColorAvailable()) {
            // No aplicamos DynamicColors.applyToActivitiesIfAvailable(this);
            // Si ya estaba puesto por el asistente de Android Studio, esta es la causa.
        }

        createNotificationChannel();
        
        // Inicializar el SDK de anuncios de Google
        new Thread(() -> {
            MobileAds.initialize(this, initializationStatus -> {});
        }).start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Default Channel";
            String description = "Channel for default notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("default_channel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
