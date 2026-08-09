package com.example.gestiondecompras.workers;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.gestiondecompras.R;
import com.example.gestiondecompras.activities.MainActivity;
import com.example.gestiondecompras.database.AppDatabase;
import com.example.gestiondecompras.models.Pedido;
import com.example.gestiondecompras.models.Tarjeta;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NotificationWorker extends Worker {

    private static final int ID_RESUMEN = 1;
    private static final int ID_ENTREGA = 100;
    private static final int ID_CORTE = 1000;
    public static final String CANAL_RECORDATORIOS = "recordatorios_diarios";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return Result.success();
        }

        AppDatabase db = AppDatabase.getInstance(context);
        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        int reminderDays = prefs.getInt("reminder_time", 3);

        long hoy = System.currentTimeMillis();
        Calendar mananaCal = Calendar.getInstance();
        mananaCal.add(Calendar.DAY_OF_YEAR, 1);
        long manana = mananaCal.getTimeInMillis();

        List<Pedido> porCobrar = db.pedidoDao().getPedidosPorCobrar(null, null, "");
        int atrasados = db.pedidoDao().getOverdueOrdersCount(hoy);
        List<Pedido> entregasManana = db.pedidoDao().getEntregasPorDia(manana);

        List<Tarjeta> tarjetas = db.tarjetaDao().getAllTarjetas();
        List<String> cortesProximos = new ArrayList<>();
        for (Tarjeta tarjeta : tarjetas) {
            if (tarjeta.getDiaCorte() == diaCorteEn(reminderDays)) {
                cortesProximos.add(mostrarTarjeta(tarjeta));
            }
        }

        for (Pedido pedido : entregasManana) {
            mostrarEntrega(pedido);
        }

        for (String corte : cortesProximos) {
            mostrarCorte(corte, reminderDays);
        }

        if (!porCobrar.isEmpty() || atrasados > 0 || !entregasManana.isEmpty() || !cortesProximos.isEmpty()) {
            mostrarResumen(porCobrar, atrasados, entregasManana, cortesProximos, reminderDays);
        }

        return Result.success();
    }

    private void mostrarResumen(List<Pedido> porCobrar, int atrasados,
                                List<Pedido> entregasManana, List<String> cortesProximos,
                                int reminderDays) {
        StringBuilder detalle = new StringBuilder();

        double totalPorCobrar = 0;
        for (Pedido p : porCobrar) {
            totalPorCobrar += p.getTotalGeneral();
        }
        if (!porCobrar.isEmpty()) {
            detalle.append(String.format(Locale.getDefault(),
                    "• %s: %d (RD$ %,.2f)\n",
                    getApplicationContext().getString(R.string.notif_pedidos_por_cobrar),
                    porCobrar.size(), totalPorCobrar));
        }
        if (atrasados > 0) {
            detalle.append(String.format(Locale.getDefault(), "• %s: %d\n",
                    getApplicationContext().getString(R.string.notif_pedidos_atrasados), atrasados));
        }
        if (!entregasManana.isEmpty()) {
            StringBuilder nombres = new StringBuilder();
            for (Pedido p : entregasManana) {
                if (nombres.length() > 0) nombres.append(", ");
                nombres.append(p.getClienteNombre());
            }
            detalle.append(String.format(Locale.getDefault(), "• %s: %d (%s)\n",
                    getApplicationContext().getString(R.string.notif_entregas_manana),
                    entregasManana.size(), nombres));
        }
        for (String corte : cortesProximos) {
            detalle.append(String.format(Locale.getDefault(), "• %s: %s\n",
                    getApplicationContext().getString(R.string.notif_corte_tarjeta, reminderDays), corte));
        }

        NotificationCompat.Builder builder = base()
                .setContentTitle(getApplicationContext().getString(R.string.notif_resumen_title))
                .setContentText(detalle.toString().trim())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(detalle.toString().trim()))
                .setOngoing(true)
                .setAutoCancel(false);

        notificar(ID_RESUMEN, builder);
    }

    private void mostrarEntrega(Pedido pedido) {
        NotificationCompat.Builder builder = base()
                .setContentTitle(getApplicationContext().getString(R.string.notif_entrega_title))
                .setContentText(getApplicationContext().getString(R.string.notif_entrega_text, pedido.getClienteNombre()));
        notificar(ID_ENTREGA + (int) pedido.getId(), builder);
    }

    private void mostrarCorte(String tarjeta, int reminderDays) {
        NotificationCompat.Builder builder = base()
                .setContentTitle(getApplicationContext().getString(R.string.notif_corte_title))
                .setContentText(getApplicationContext().getString(R.string.notif_corte_text, tarjeta, reminderDays));
        notificar(ID_CORTE + tarjeta.hashCode() % 1000, builder);
    }

    private NotificationCompat.Builder base() {
        Context context = getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(context, CANAL_RECORDATORIOS)
                .setSmallIcon(R.drawable.ic_notifications)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
    }

    private void notificar(int id, NotificationCompat.Builder builder) {
        NotificationManagerCompat.from(getApplicationContext()).notify(id, builder.build());
    }

    private int diaCorteEn(int dias) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, dias);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    private String mostrarTarjeta(Tarjeta tarjeta) {
        if (tarjeta.getAlias() != null && !tarjeta.getAlias().isEmpty()) {
            return tarjeta.getAlias();
        }
        return tarjeta.getBanco() != null ? tarjeta.getBanco() : "Tarjeta";
    }

    public static void crearCanales(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        NotificationChannel canal = new NotificationChannel(CANAL_RECORDATORIOS,
                context.getString(R.string.notif_canal_nombre), NotificationManager.IMPORTANCE_HIGH);
        canal.setDescription(context.getString(R.string.notif_canal_desc));
        manager.createNotificationChannel(canal);
    }
}