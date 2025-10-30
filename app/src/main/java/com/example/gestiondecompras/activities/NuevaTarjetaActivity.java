package com.example.gestiondecompras.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.gestiondecompras.databinding.ActivityNuevaTarjetaBinding;
import com.example.gestiondecompras.models.Tarjeta;
import com.example.gestiondecompras.viewmodels.NuevaTarjetaViewModel;

public class NuevaTarjetaActivity extends AppCompatActivity {

    private ActivityNuevaTarjetaBinding binding;
    private NuevaTarjetaViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNuevaTarjetaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(NuevaTarjetaViewModel.class);

        setupToolbar();
        setupSaveButton();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSaveButton() {
        binding.btnGuardar.setOnClickListener(v -> saveTarjeta());
    }

    private void saveTarjeta() {
        String banco = binding.etBanco.getText().toString();
        String alias = binding.etAlias.getText().toString();
        double limite = Double.parseDouble(binding.etLimite.getText().toString());
        int diaCorte = Integer.parseInt(binding.etDiaCorte.getText().toString());
        String fechaVencimiento = binding.etFechaVencimiento.getText().toString();
        String notas = binding.etNotas.getText().toString();

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.banco = banco;
        tarjeta.alias = alias;
        tarjeta.limite = limite;
        tarjeta.diaCorte = diaCorte;
        tarjeta.fechaVencimiento = fechaVencimiento;
        tarjeta.notas = notas;

        viewModel.saveTarjeta(tarjeta);

        Toast.makeText(this, "Tarjeta guardada", Toast.LENGTH_SHORT).show();
        finish();
    }
}