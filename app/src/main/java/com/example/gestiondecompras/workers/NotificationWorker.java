package com.example.gestiondecompras.workers;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.gestiondecompras.R;
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
                showNotification("Corte de tarjeta", "El corte de la tarjeta " + tarjeta.alias + " es en " + reminderTime + " días.", (int) tarjeta.id + 1000);
            }
        }
    }

    private void showNotification(String title, String content, int notificationId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "default_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(notificationId, builder.build());
    }
}
