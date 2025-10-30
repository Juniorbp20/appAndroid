package com.example.gestiondecompras.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.gestiondecompras.R;
import com.example.gestiondecompras.databinding.ActivityNuevoPedidoBinding;
import com.example.gestiondecompras.models.Cliente;
import com.example.gestiondecompras.models.Pedido;
import com.example.gestiondecompras.models.Tarjeta;
import com.example.gestiondecompras.models.Tienda;
import com.example.gestiondecompras.viewmodels.NuevoPedidoViewModel;

import java.util.Calendar;
import java.util.List;

public class NuevoPedidoActivity extends AppCompatActivity {

    private ActivityNuevoPedidoBinding binding;
    private NuevoPedidoViewModel viewModel;
    private Calendar fechaRegistro = Calendar.getInstance();
    private Calendar fechaEntrega = Calendar.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNuevoPedidoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(NuevoPedidoViewModel.class);

        setupToolbar();
        setupSpinners();
        setupDatePickers();
        setupSaveButton();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupSpinners() {
        viewModel.getClientes().observe(this, clientes -> {
            if (clientes != null) {
                ArrayAdapter<Cliente> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, clientes);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerCliente.setAdapter(adapter);
            }
        });

        viewModel.getTiendas().observe(this, tiendas -> {
            if (tiendas != null) {
                ArrayAdapter<Tienda> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tiendas);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerTienda.setAdapter(adapter);
            }
        });

        viewModel.getTarjetas().observe(this, tarjetas -> {
            if (tarjetas != null) {
                ArrayAdapter<Tarjeta> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, tarjetas);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                binding.spinnerTarjeta.setAdapter(adapter);
            }
        });
    }

    private void setupDatePickers() {
        binding.btnFechaRegistro.setOnClickListener(v -> showDatePickerDialog(fechaRegistro, binding.btnFechaRegistro));
        binding.btnFechaEntrega.setOnClickListener(v -> showDatePickerDialog(fechaEntrega, binding.btnFechaEntrega));
        binding.btnNuevaTarjeta.setOnClickListener(v -> startActivity(new Intent(this, NuevaTarjetaActivity.class)));
    }

    private void showDatePickerDialog(Calendar calendar, View view) {
        DatePickerDialog dialog = new DatePickerDialog(this,
                (datePicker, year, month, day) -> {
                    calendar.set(year, month, day);
                    ((android.widget.Button) view).setText(String.format("%d/%d/%d", day, month + 1, year));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void setupSaveButton() {
        binding.btnGuardar.setOnClickListener(v -> savePedido());
    }

    private void savePedido() {
        // Get data from form
        Cliente cliente = (Cliente) binding.spinnerCliente.getSelectedItem();
        Tienda tienda = (Tienda) binding.spinnerTienda.getSelectedItem();
        Tarjeta tarjeta = (Tarjeta) binding.spinnerTarjeta.getSelectedItem();
        double monto = Double.parseDouble(binding.etMontoCompra.getText().toString());
        double ganancia = Double.parseDouble(binding.etGanancia.getText().toString());
        boolean isGananciaFija = binding.rbGananciaFija.isChecked();

        // Calculate profit
        double totalGanancia;
        if (isGananciaFija) {
            totalGanancia = ganancia;
        } else {
            totalGanancia = monto * (ganancia / 100);
        }

        double totalGeneral = monto + totalGanancia;

        // Validations
        if (tarjeta.limite > 0 && (tarjeta.deudaActual + monto) > tarjeta.limite) {
            Toast.makeText(this, "El monto de la compra supera el límite de la tarjeta", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] parts = tarjeta.fechaVencimiento.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]) + 2000;
        Calendar expiryDate = Calendar.getInstance();
        expiryDate.set(Calendar.YEAR, year);
        expiryDate.set(Calendar.MONTH, month);
        if (expiryDate.before(Calendar.getInstance())) {
            Toast.makeText(this, "La tarjeta está vencida", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Pedido object
        Pedido pedido = new Pedido();
        pedido.clienteId = cliente.id;
        pedido.tiendaId = tienda.id;
        pedido.tarjetaId = tarjeta.id;
        pedido.montoCompra = monto;
        pedido.ganancia = totalGanancia;
        pedido.totalGeneral = totalGeneral;
        pedido.fechaRegistroEpoch = fechaRegistro.getTimeInMillis();
        pedido.fechaEntregaEpoch = fechaEntrega.getTimeInMillis();
        pedido.notas = binding.etNotas.getText().toString();
        pedido.estado = Pedido.ESTADO_PENDIENTE;

        // Save pedido
        viewModel.savePedido(pedido, tarjeta);

        Toast.makeText(this, "Pedido guardado", Toast.LENGTH_SHORT).show();
        finish();
    }
}