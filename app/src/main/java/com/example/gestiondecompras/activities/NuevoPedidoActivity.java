package com.example.gestiondecompras.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

public class NuevoPedidoActivity extends AppCompatActivity {

    private ActivityNuevoPedidoBinding binding;
    private NuevoPedidoViewModel viewModel;
    private Calendar fechaRegistro = Calendar.getInstance();
    private Calendar fechaEntrega = Calendar.getInstance();
    private final TextWatcher totalWatcher = new TextWatcher() {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(Editable s) { updateTotalGeneral(); }
    };

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
        setupInputWatchers();
        updateTotalGeneral();
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

    private void setupInputWatchers() {
        if (!binding.rbGananciaFija.isChecked() && !binding.rbGananciaPorcentaje.isChecked()) {
            binding.rbGananciaFija.setChecked(true);
        }
        binding.etMontoCompra.addTextChangedListener(totalWatcher);
        binding.etGanancia.addTextChangedListener(totalWatcher);
        binding.rgGanancia.setOnCheckedChangeListener((group, checkedId) -> updateTotalGeneral());
    }

    private void updateTotalGeneral() {
        String montoTexto = binding.etMontoCompra.getText() != null
                ? binding.etMontoCompra.getText().toString().trim() : "";
        String gananciaTexto = binding.etGanancia.getText() != null
                ? binding.etGanancia.getText().toString().trim() : "";

        if (montoTexto.isEmpty() || gananciaTexto.isEmpty()) {
            binding.tvTotalGeneral.setText(getString(R.string.total_general_amount, 0.0));
            return;
        }

        try {
            double monto = Double.parseDouble(montoTexto);
            double ganancia = Double.parseDouble(gananciaTexto);
            boolean isGananciaFija = binding.rbGananciaFija.isChecked();
            double totalGanancia = isGananciaFija ? ganancia : monto * (ganancia / 100);
            double totalGeneral = monto + totalGanancia;
            binding.tvTotalGeneral.setText(getString(R.string.total_general_amount, totalGeneral));
        } catch (NumberFormatException ex) {
            binding.tvTotalGeneral.setText(getString(R.string.total_general_amount, 0.0));
        }
    }

    private void savePedido() {
        Cliente cliente = (Cliente) binding.spinnerCliente.getSelectedItem();
        Tienda tienda = (Tienda) binding.spinnerTienda.getSelectedItem();
        Tarjeta tarjeta = (Tarjeta) binding.spinnerTarjeta.getSelectedItem();

        if (cliente == null || tienda == null || tarjeta == null) {
            Toast.makeText(this, "Selecciona cliente, tienda y tarjeta.", Toast.LENGTH_SHORT).show();
            return;
        }

        String montoTexto = binding.etMontoCompra.getText().toString().trim();
        String gananciaTexto = binding.etGanancia.getText().toString().trim();
        if (montoTexto.isEmpty() || gananciaTexto.isEmpty()) {
            Toast.makeText(this, "Completa monto de compra y ganancia.", Toast.LENGTH_SHORT).show();
            return;
        }

        double monto;
        double ganancia;
        try {
            monto = Double.parseDouble(montoTexto);
            ganancia = Double.parseDouble(gananciaTexto);
        } catch (NumberFormatException ex) {
            Toast.makeText(this, "Verifica los valores numericos ingresados.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isGananciaFija = binding.rbGananciaFija.isChecked();
        double totalGanancia = isGananciaFija ? ganancia : monto * (ganancia / 100);
        double totalGeneral = monto + totalGanancia;
        binding.tvTotalGeneral.setText(getString(R.string.total_general_amount, totalGeneral));

        if (tarjeta.getLimiteCredito() > 0 && (tarjeta.getDeudaActual() + monto) > tarjeta.getLimiteCredito()) {
            Toast.makeText(this, "El monto de la compra supera el limite de la tarjeta.", Toast.LENGTH_SHORT).show();
            return;
        }

        String fechaVigencia = tarjeta.getFechaVencimiento();
        if (fechaVigencia == null || fechaVigencia.trim().isEmpty()) {
            Toast.makeText(this, "La tarjeta no tiene fecha de vencimiento.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] partes = fechaVigencia.split("[-/]");
        if (partes.length != 2) {
            Toast.makeText(this, "Formato de vencimiento invalido (usa MM-YY).", Toast.LENGTH_SHORT).show();
            return;
        }

        int month;
        int year;
        try {
            month = Integer.parseInt(partes[0]);
            year = Integer.parseInt(partes[1]);
        } catch (NumberFormatException ex) {
            Toast.makeText(this, "Formato de vencimiento invalido (usa MM-YY).", Toast.LENGTH_SHORT).show();
            return;
        }

        if (month < 1 || month > 12) {
            Toast.makeText(this, "Mes de vencimiento fuera de rango.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (year < 100) {
            year += 2000;
        }

        Calendar expiryDate = Calendar.getInstance();
        expiryDate.set(Calendar.YEAR, year);
        expiryDate.set(Calendar.MONTH, month - 1);
        expiryDate.set(Calendar.DAY_OF_MONTH, expiryDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        expiryDate.set(Calendar.HOUR_OF_DAY, 23);
        expiryDate.set(Calendar.MINUTE, 59);
        expiryDate.set(Calendar.SECOND, 59);
        expiryDate.set(Calendar.MILLISECOND, 999);
        if (expiryDate.before(Calendar.getInstance())) {
            Toast.makeText(this, "La tarjeta esta vencida.", Toast.LENGTH_SHORT).show();
            return;
        }

        Pedido pedido = new Pedido();
        pedido.clienteId = cliente.id;
        pedido.setClienteNombre(cliente.getNombre());
        pedido.tiendaId = tienda.id;
        pedido.setTienda(tienda.getNombre());
        pedido.tarjetaId = tarjeta.id;
        pedido.setTarjetaAlias(tarjeta.getAlias());
        pedido.montoCompra = monto;
        pedido.ganancia = totalGanancia;
        pedido.totalGeneral = totalGeneral;
        pedido.fechaRegistroEpoch = fechaRegistro.getTimeInMillis();
        pedido.fechaEntregaEpoch = fechaEntrega.getTimeInMillis();
        pedido.notas = binding.etNotas.getText().toString();
        pedido.estado = Pedido.ESTADO_PENDIENTE;

        viewModel.savePedido(pedido, tarjeta);
        Toast.makeText(this, "Pedido guardado", Toast.LENGTH_SHORT).show();
        finish();
    }
}
