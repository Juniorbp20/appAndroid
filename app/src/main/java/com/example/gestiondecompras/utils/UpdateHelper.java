package com.example.gestiondecompras.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

import java.io.File;
import java.util.Locale;

public class UpdateHelper {

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
            destino.delete();
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        request.setTitle(activity.getString(R.string.descargando_actualizacion));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationUri(Uri.fromFile(destino));

        DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        if (dm == null) {
            Toast.makeText(activity, R.string.error_descarga, Toast.LENGTH_LONG).show();
            return;
        }

        long downloadId;
        try {
            downloadId = dm.enqueue(request);
        } catch (Exception e) {
            Toast.makeText(activity, R.string.error_descarga, Toast.LENGTH_LONG).show();
            return;
        }

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_download_progress, null);
        LinearProgressIndicator progressBar = dialogView.findViewById(R.id.progress_bar);
        TextView tvProgressPercent = dialogView.findViewById(R.id.tv_progress_percent);

        AlertDialog progressDialog = new AlertDialog.Builder(activity)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        progressDialog.show();

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable checkRunnable = new Runnable() {
            int intentosFail = 0;

            @Override
            public void run() {
                if (activity.isFinishing() || activity.isDestroyed()) {
                    return;
                }

                DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
                try (Cursor cursor = dm.query(query)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int statusIdx = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int downloadedIdx = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                        int totalIdx = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);

                        int status = cursor.getInt(statusIdx);
                        long bytesDownloaded = cursor.getLong(downloadedIdx);
                        long bytesTotal = cursor.getLong(totalIdx);

                        if (bytesTotal > 0) {
                            int progress = (int) ((bytesDownloaded * 100) / bytesTotal);
                            progressBar.setProgress(progress);
                            float downloadedMb = bytesDownloaded / (1024f * 1024f);
                            float totalMb = bytesTotal / (1024f * 1024f);
                            tvProgressPercent.setText(String.format(Locale.getDefault(), "%d%% (%.1f MB / %.1f MB)", progress, downloadedMb, totalMb));
                        }

                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            progressDialog.dismiss();
                            instalarApk(activity, destino);
                            return;
                        } else if (status == DownloadManager.STATUS_FAILED) {
                            progressDialog.dismiss();
                            Toast.makeText(activity, R.string.error_descarga, Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                } catch (Exception ignored) {
                }

                if (intentosFail++ < 200) {
                    handler.postDelayed(this, 500);
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(activity, R.string.error_instalacion, Toast.LENGTH_LONG).show();
                }
            }
        };

        handler.post(checkRunnable);
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
