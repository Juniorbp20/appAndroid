package com.example.gestiondecompras.workers;

import static com.example.gestiondecompras.R.*;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Pedido;
import com.example.gestiondecompras.models.Tarjeta;
import java.util.Calendar;
import java.util.List;

public class NotificationWorker extends Worker {

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppDatabase db = AppDatabase.getInstance(getApplicationContext());
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        int reminderTime = sharedPreferences.getInt("reminder_time", 3);

        checkUpcomingDeliveries(db);
        checkCardDueDates(db, reminderTime);

        return Result.success();
    }

    private void checkUpcomingDeliveries(AppDatabase db) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        long tomorrow = calendar.getTimeInMillis();

        List<Pedido> upcomingPedidos = db.pedidoDao().pedidosPorDia(tomorrow);

        for (Pedido pedido : upcomingPedidos) {
            if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            showNotification("Entrega de pedido", "El pedido para " + pedido.clienteNombre + " se entrega mañana.", (int) pedido.id);
        }
    }

    private void checkCardDueDates(AppDatabase db, int reminderTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, reminderTime);
        int upcomingDueDate = calendar.get(Calendar.DAY_OF_MONTH);

        List<Tarjeta> tarjetas = db.tarjetaDao().getAllTarjetas();

        for (Tarjeta tarjeta : tarjetas) {
            if (tarjeta.diaCorte == upcomingDueDate) {
                if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                showNotification("Corte de tarjeta", "El corte de la tarjeta " + tarjeta.alias + " es en " + reminderTime + " días.", (int) tarjeta.id + 1000);
            }
        }
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void showNotification(String title, String content, int notificationId) {
        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(getApplicationContext(), "default_channel")
                .setSmallIcon(drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        
        // Even if we check in MainActivity, it's safer to use NotificationManagerCompat which handles 
        // the permission check internally on newer versions.
        notificationManager.notify(notificationId, builder.build());
    }
}
