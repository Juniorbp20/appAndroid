package com.example.gestiondecompras.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gestiondecompras.R;

import com.example.gestiondecompras.adapters.TarjetasAdapter;
import com.example.gestiondecompras.database.DatabaseHelper;
import com.example.gestiondecompras.databinding.ActivityTarjetasBinding;
import com.example.gestiondecompras.models.Tarjeta;

import java.util.ArrayList;
import java.util.List;

public class TarjetasActivity extends AppCompatActivity implements TarjetasAdapter.OnTarjetaClickListener {

    private ActivityTarjetasBinding binding;
    private ListaTarjetasViewModel viewModel;
    private TarjetasAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTarjetasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ListaTarjetasViewModel.class);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        setupFab();
        observeViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadTarjetas();
    }

    private void setupRecyclerView() {
        binding.rvTarjetas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TarjetasAdapter(this);
        binding.rvTarjetas.setAdapter(adapter);
    }

    private void setupFab() {
        binding.btnNuevaTarjeta.setOnClickListener(v ->
                startActivity(new Intent(this, NuevaTarjetaActivity.class)));
    }

    private void observeViewModel() {
        viewModel.getTarjetas().observe(this, tarjetas -> {
            if (tarjetas != null) {
                adapter.actualizarLista(tarjetas);
                binding.tvEmpty.setVisibility(tarjetas.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onTarjetaClick(Tarjeta tarjeta) {
        mostrarDialogoPago(tarjeta);
    }

    @Override
    public void onTarjetaLongClick(Tarjeta tarjeta) {
        mostrarOpcionesTarjeta(tarjeta);
    }

    private void mostrarOpcionesTarjeta(Tarjeta tarjeta) {
        CharSequence[] opciones = {
                getString(R.string.dialog_pago_tarjeta_title),
                getString(R.string.dialog_delete_tarjeta_title)
        };

        new AlertDialog.Builder(this)
                .setTitle(tarjeta.alias != null && !tarjeta.alias.isEmpty() ? tarjeta.alias : tarjeta.banco)
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        mostrarDialogoPago(tarjeta);
                    } else if (which == 1) {
                        confirmarEliminarTarjeta(tarjeta);
                    }
                })
                .setNegativeButton(R.string.accion_cancelar, null)
                .show();
    }

    private void mostrarDialogoPago(Tarjeta tarjeta) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_pago_tarjeta);
        android.widget.TextView tvNombre = dialog.findViewById(R.id.tvTarjetaNombre);
        com.google.android.material.textfield.TextInputEditText etMonto = dialog.findViewById(R.id.etMontoPagoTarjeta);
        android.widget.Button btnRegistrar = dialog.findViewById(R.id.btnConfirmarPagoTarjeta);

        String titulo = tarjeta.banco;
        if (tarjeta.alias != null && !tarjeta.alias.isEmpty()) {
            titulo += " - " + tarjeta.alias;
        }
        tvNombre.setText(titulo);

        btnRegistrar.setOnClickListener(v -> {
            try {
                double pago = Double.parseDouble(String.valueOf(etMonto.getText()));
                if (pago <= 0) {
                    throw new NumberFormatException();
                }
                tarjeta.deudaActual -= pago;
                viewModel.updateTarjeta(tarjeta);
                Toast.makeText(this, R.string.toast_tarjeta_pagada, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } catch (NumberFormatException ex) {
                Toast.makeText(this, R.string.dialog_pago_tarjeta_error, Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void confirmarEliminarTarjeta(Tarjeta tarjeta) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_tarjeta_title)
                .setMessage(R.string.dialog_delete_tarjeta_message)
                .setPositiveButton(R.string.accion_eliminar, (dialogInterface, which) -> viewModel.deleteTarjeta(tarjeta))
                .setNegativeButton(R.string.accion_cancelar, null)
                .show();
    }
}
