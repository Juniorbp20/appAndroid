package com.example.gestiondecompras.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.gestiondecompras.databinding.ActivityNuevoClienteBinding;
import com.example.gestiondecompras.models.Cliente;
import com.example.gestiondecompras.viewmodels.NuevoClienteViewModel;

public class NuevoClienteActivity extends AppCompatActivity {

    private ActivityNuevoClienteBinding binding;
    private NuevoClienteViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNuevoClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(NuevoClienteViewModel.class);

        setupToolbar();
        setupSaveButton();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSaveButton() {
        binding.btnGuardar.setOnClickListener(v -> saveCliente());
    }

    private void saveCliente() {
        String nombre = binding.etNombre.getText().toString();
        String apellido = binding.etApellido.getText().toString();
        String telefono = binding.etTelefono.getText().toString();
        String direccion = binding.etDireccion.getText().toString();
        boolean activo = binding.cbActivo.isChecked();

        Cliente cliente = new Cliente();
        cliente.nombre = nombre;
        cliente.apellido = apellido;
        cliente.telefono = telefono;
        cliente.direccion = direccion;
        cliente.activo = activo;

        viewModel.saveCliente(cliente);

        Toast.makeText(this, "Cliente guardado", Toast.LENGTH_SHORT).show();
        finish();
    }
}