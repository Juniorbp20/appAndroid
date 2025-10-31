package com.example.gestiondecompras.activities;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gestiondecompras.R;
import com.example.gestiondecompras.adapters.PedidosAdapter;
import com.example.gestiondecompras.databinding.ActivityReportesBinding;
import com.example.gestiondecompras.models.Pedido;
import com.example.gestiondecompras.viewmodels.ReportesViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportesActivity extends AppCompatActivity {

    private ActivityReportesBinding binding;
    private ReportesViewModel viewModel;
    private PedidosAdapter todosAdapter;
    private PedidosAdapter pagadosAdapter;
    private List<Pedido> pedidosPagadosActual = Collections.emptyList();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityReportesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ReportesViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setTitle(R.string.reportes_title);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerViews();
        observeViewModel();
        binding.btnExportPagados.setOnClickListener(v -> exportPagados());
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadReportes();
    }

    private void setupRecyclerViews() {
        todosAdapter = new PedidosAdapter(null);
        binding.rvReportePedidos.setLayoutManager(new LinearLayoutManager(this));
        binding.rvReportePedidos.setAdapter(todosAdapter);
        binding.rvReportePedidos.setClickable(false);
        binding.rvReportePedidos.setFocusable(false);

        pagadosAdapter = new PedidosAdapter(null);
        binding.rvPagados.setLayoutManager(new LinearLayoutManager(this));
        binding.rvPagados.setAdapter(pagadosAdapter);
        binding.rvPagados.setClickable(false);
        binding.rvPagados.setFocusable(false);
    }

    private void observeViewModel() {
        viewModel.getTotalCobrado().observe(this, total -> {
            if (total != null) {
                binding.tvTotalCobrado.setText(formatoMoneda(total));
            }
        });

        viewModel.getTotalPendiente().observe(this, total -> {
            if (total != null) {
                binding.tvTotalPendiente.setText(formatoMoneda(total));
            }
        });

        viewModel.getVentasGeneradas().observe(this, total -> {
            if (total != null) {
                binding.tvTotalVentas.setText(formatoMoneda(total));
            }
        });

        viewModel.getGananciaProyectada().observe(this, total -> {
            if (total != null) {
                binding.tvTotalGanancia.setText(formatoMoneda(total));
            }
        });

        viewModel.getPedidos().observe(this, pedidos -> todosAdapter.actualizarLista(pedidos != null ? pedidos : Collections.emptyList()));

        viewModel.getPedidosPagados().observe(this, pagados -> {
            pedidosPagadosActual = pagados != null ? pagados : Collections.emptyList();
            pagadosAdapter.actualizarLista(pedidosPagadosActual);
            binding.tvPagadosVacio.setVisibility(pedidosPagadosActual.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    private String formatoMoneda(double valor) {
        return String.format(Locale.getDefault(), "RD$ %,.2f", valor);
    }

    private void exportPagados() {
        if (pedidosPagadosActual == null || pedidosPagadosActual.isEmpty()) {
            Toast.makeText(this, R.string.reportes_pagados_vacios, Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument document = new PdfDocument();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int pageWidth = 595;
        int pageHeight = 842;
        int margin = 40;
        int y = margin + 20;
        int pageNumber = 1;

        paint.setTextSize(16f);
        paint.setFakeBoldText(true);
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        canvas.drawText(getString(R.string.app_name), margin, y, paint);
        paint.setFakeBoldText(false);
        paint.setTextSize(14f);
        y += 24;
        canvas.drawText(getString(R.string.reportes_pagados_title), margin, y, paint);
        y += 24;

        @SuppressLint("DefaultLocale")
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        for (Pedido pedido : pedidosPagadosActual) {
            if (y > pageHeight - margin) {
                document.finishPage(page);
                pageNumber++;
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = margin + 20;
                paint.setTextSize(16f);
                paint.setFakeBoldText(true);
                canvas.drawText(getString(R.string.app_name), margin, y, paint);
                paint.setFakeBoldText(false);
                paint.setTextSize(14f);
                y += 24;
                canvas.drawText(getString(R.string.reportes_pagados_title), margin, y, paint);
                y += 24;
            }
            canvas.drawText("Cliente: " + pedido.getClienteNombre(), margin, y, paint);
            y += 16;
            canvas.drawText("Tienda: " + pedido.getTienda(), margin, y, paint);
            y += 16;
            canvas.drawText(String.format(Locale.getDefault(), "Total: RD$ %,.2f", pedido.getTotalGeneral()), margin, y, paint);
            y += 16;
            String fecha = pedido.getFechaRegistro() != null ? sdf.format(pedido.getFechaRegistro()) : "";
            canvas.drawText("Registrado: " + fecha, margin, y, paint);
            y += 24;
        }

        document.finishPage(page);

        try {
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (dir == null) {
                Toast.makeText(this, R.string.reportes_pdf_error, Toast.LENGTH_SHORT).show();
                document.close();
                return;
            }
            if (!dir.exists() && !dir.mkdirs()) {
                Toast.makeText(this, R.string.reportes_pdf_error, Toast.LENGTH_SHORT).show();
                document.close();
                return;
            }
            String fileName = "pagados_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";
            File file = new File(dir, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            fos.close();
            Toast.makeText(this, getString(R.string.reportes_pdf_generado, file.getAbsolutePath()), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, R.string.reportes_pdf_error, Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }
}
