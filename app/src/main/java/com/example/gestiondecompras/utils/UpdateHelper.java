package com.example.gestiondecompras.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.example.gestiondecompras.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UpdateHelper {

    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private UpdateHelper() {
    }

    public static void checkAndPrompt(Activity activity, boolean notifyIfUpdated) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        if (notifyIfUpdated) {
            Toast.makeText(activity, R.string.comprobando_actualizaciones, Toast.LENGTH_SHORT).show();
        }

        UpdateChecker.check(new UpdateChecker.Callback() {
            @Override
            public void onUpdateAvailable(String version, String apkUrl) {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                mostrarDialogoConfirmacion(activity, version, apkUrl);
            }

            @Override
            public void onNoUpdate() {
                if (notifyIfUpdated && !activity.isFinishing() && !activity.isDestroyed()) {
                    Toast.makeText(activity, R.string.app_actualizada, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private static void mostrarDialogoConfirmacion(Activity activity, String version, String apkUrl) {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.actualizacion_disponible)
                .setMessage(activity.getString(R.string.actualizacion_mensaje, version))
                .setPositiveButton(R.string.actualizar, (d, w) -> iniciarDescargaConProgreso(activity, apkUrl))
                .setNegativeButton(R.string.ahora_no, null)
                .show();
    }

    public static void iniciarDescargaConProgreso(Activity activity, String apkUrl) {
        File destino = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "gestion-compras.apk");
        if (destino.exists()) {
            //noinspection ResultOfMethodCallIgnored
            destino.delete();
        }

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_download_progress, null);
        LinearProgressIndicator progressBar = dialogView.findViewById(R.id.progress_bar);
        TextView tvProgressPercent = dialogView.findViewById(R.id.tv_progress_percent);

        AlertDialog progressDialog = new AlertDialog.Builder(activity)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        progressDialog.show();

        descargarDirecto(activity, apkUrl, destino, progressDialog, progressBar, tvProgressPercent);
    }

    private static void descargarDirecto(Activity activity, String apkUrl, File destino, AlertDialog progressDialog, LinearProgressIndicator progressBar, TextView tvProgressPercent) {
        EXECUTOR.execute(() -> {
            boolean exito = false;
            try {
                String currentUrl = apkUrl;
                HttpURLConnection conn = null;
                int redirects = 0;

                while (redirects < 5) {
                    conn = (HttpURLConnection) new URL(currentUrl).openConnection();
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    conn.setRequestProperty("User-Agent", "GestionDeComprasApp");
                    conn.setRequestProperty("Accept-Encoding", "identity");
                    conn.setInstanceFollowRedirects(true);

                    int code = conn.getResponseCode();
                    if (code == HttpURLConnection.HTTP_MOVED_PERM || code == HttpURLConnection.HTTP_MOVED_TEMP || code == 307 || code == 308) {
                        String location = conn.getHeaderField("Location");
                        if (location != null && !location.isEmpty()) {
                            currentUrl = location;
                            conn.disconnect();
                            redirects++;
                            continue;
                        }
                    }
                    break;
                }

                if (conn != null && conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    long totalBytes = conn.getContentLengthLong();

                    try (InputStream in = new BufferedInputStream(conn.getInputStream(), 128 * 1024);
                         FileOutputStream out = new FileOutputStream(destino)) {

                        byte[] buffer = new byte[128 * 1024];
                        int bytesRead;
                        long totalDownloaded = 0;
                        long lastUpdate = 0;

                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                            totalDownloaded += bytesRead;

                            long currentDownloaded = totalDownloaded;
                            long now = System.currentTimeMillis();

                            if (now - lastUpdate > 50 || currentDownloaded == totalBytes) {
                                lastUpdate = now;
                                MAIN_HANDLER.post(() -> {
                                    if (activity.isFinishing() || activity.isDestroyed()) return;
                                    if (totalBytes > 0) {
                                        int progress = (int) ((currentDownloaded * 100) / totalBytes);
                                        progressBar.setProgress(progress);
                                        float downloadedMb = currentDownloaded / (1024f * 1024f);
                                        float totalMb = totalBytes / (1024f * 1024f);
                                        tvProgressPercent.setText(String.format(Locale.getDefault(), "%d%% (%.1f MB / %.1f MB)", progress, downloadedMb, totalMb));
                                    } else {
                                        progressBar.setIndeterminate(true);
                                        float downloadedMb = currentDownloaded / (1024f * 1024f);
                                        tvProgressPercent.setText(String.format(Locale.getDefault(), "%.1f MB", downloadedMb));
                                    }
                                });
                            }
                        }
                        out.flush();
                        exito = totalDownloaded > 0;
                    }
                    conn.disconnect();
                }
            } catch (Exception e) {
                exito = false;
            }

            boolean finalExito = exito;
            MAIN_HANDLER.post(() -> {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                progressDialog.dismiss();
                if (finalExito && destino.exists() && destino.length() > 0) {
                    instalarApk(activity, destino);
                } else {
                    Toast.makeText(activity, R.string.error_descarga, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    public static void instalarApk(Context context, File apk) {
        if (apk == null || !apk.exists()) {
            Toast.makeText(context, R.string.error_instalacion, Toast.LENGTH_LONG).show();
            return;
        }

        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", apk);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, R.string.error_instalacion, Toast.LENGTH_LONG).show();
        }
    }
}
