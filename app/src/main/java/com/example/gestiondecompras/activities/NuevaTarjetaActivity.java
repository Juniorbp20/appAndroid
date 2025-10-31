package com.example.gestiondecompras.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.gestiondecompras.R;
import com.example.gestiondecompras.databinding.ActivityNuevaTarjetaBinding;
import com.example.gestiondecompras.models.Tarjeta;
import com.example.gestiondecompras.viewmodels.NuevaTarjetaViewModel;
import com.google.android.material.textfield.TextInputEditText;

public class NuevaTarjetaActivity extends AppCompatActivity {

    public static final String EXTRA_TARJETA_ID = "extra_tarjeta_id";
    public static final String EXTRA_TARJETA_BANCO = "extra_tarjeta_banco";
    public static final String EXTRA_TARJETA_ALIAS = "extra_tarjeta_alias";
    public static final String EXTRA_TARJETA_LIMITE = "extra_tarjeta_limite";
    public static final String EXTRA_TARJETA_DIA_CORTE = "extra_tarjeta_dia_corte";
    public static final String EXTRA_TARJETA_VENCIMIENTO = "extra_tarjeta_vencimiento";
    public static final String EXTRA_TARJETA_NOTAS = "extra_tarjeta_notas";
    public static final String EXTRA_TARJETA_DEUDA = "extra_tarjeta_deuda";

    private ActivityNuevaTarjetaBinding binding;
    private NuevaTarjetaViewModel viewModel;
    private long tarjetaIdEdicion = 0L;
    private double deudaOriginal = 0d;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNuevaTarjetaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(NuevaTarjetaViewModel.class);

        setupToolbar();
        setupSaveButton();
        cargarDatosEdicion(getIntent());
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSaveButton() {
        binding.btnGuardar.setOnClickListener(v -> saveTarjeta());
    }

    private void cargarDatosEdicion(Intent intent) {
        if (intent == null) {
            return;
        }
        tarjetaIdEdicion = intent.getLongExtra(EXTRA_TARJETA_ID, 0L);
        if (tarjetaIdEdicion <= 0L) {
            return;
        }

        binding.toolbar.setTitle(R.string.editar_tarjeta_title);
        binding.btnGuardar.setText(R.string.guardar_cambios);

        binding.etBanco.setText(intent.getStringExtra(EXTRA_TARJETA_BANCO));
        binding.etAlias.setText(intent.getStringExtra(EXTRA_TARJETA_ALIAS));
        binding.etLimite.setText(valorNumerico(intent, EXTRA_TARJETA_LIMITE));
        binding.etDiaCorte.setText(valorEntero(intent, EXTRA_TARJETA_DIA_CORTE));
        binding.etFechaVencimiento.setText(intent.getStringExtra(EXTRA_TARJETA_VENCIMIENTO));
        binding.etNotas.setText(intent.getStringExtra(EXTRA_TARJETA_NOTAS));
        deudaOriginal = intent.getDoubleExtra(EXTRA_TARJETA_DEUDA, 0d);
    }

    private CharSequence valorNumerico(Intent intent, String extra) {
        if (!intent.hasExtra(extra)) {
            return "";
        }
        double valor = intent.getDoubleExtra(extra, 0d);
        if (valor == 0d) {
            return "";
        }
        return String.valueOf(valor);
    }

    private CharSequence valorEntero(Intent intent, String extra) {
        if (!intent.hasExtra(extra)) {
            return "";
        }
        int valor = intent.getIntExtra(extra, 0);
        if (valor == 0) {
            return "";
        }
        return String.valueOf(valor);
    }

    private void saveTarjeta() {
        String banco = texto(binding.etBanco);
        String alias = texto(binding.etAlias);
        String limiteTexto = texto(binding.etLimite);
        String diaCorteTexto = texto(binding.etDiaCorte);
        String fechaVencimiento = texto(binding.etFechaVencimiento);
        String notas = texto(binding.etNotas);

        if (TextUtils.isEmpty(banco) || TextUtils.isEmpty(limiteTexto) || TextUtils.isEmpty(diaCorteTexto) || TextUtils.isEmpty(fechaVencimiento)) {
            Toast.makeText(this, R.string.tarjeta_campos_obligatorios, Toast.LENGTH_SHORT).show();
            return;
        }

        double limite;
        int diaCorte;
        try {
            limite = Double.parseDouble(limiteTexto);
            diaCorte = Integer.parseInt(diaCorteTexto);
        } catch (NumberFormatException ex) {
            Toast.makeText(this, R.string.tarjeta_campos_obligatorios, Toast.LENGTH_SHORT).show();
            return;
        }

        Tarjeta tarjeta = new Tarjeta();
        tarjeta.setId(tarjetaIdEdicion);
        tarjeta.setBanco(banco);
        tarjeta.setAlias(alias);
        tarjeta.setLimiteCredito(limite);
        tarjeta.setDiaCorte(diaCorte);
        tarjeta.setFechaVencimiento(fechaVencimiento);
        tarjeta.setNotas(notas);
        tarjeta.setDeudaActual(deudaOriginal);

        viewModel.saveTarjeta(tarjeta);

        Toast.makeText(this, R.string.toast_tarjeta_guardada, Toast.LENGTH_SHORT).show();
        finish();
    }

    private String texto(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
