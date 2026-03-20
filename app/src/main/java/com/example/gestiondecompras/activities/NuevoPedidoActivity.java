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
    private long pedidoIdEdicion = 0;
    private double originalMonto = 0; // Guardamos el monto original para ediciones
    
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
        
        if (getIntent().hasExtra("pedido_id")) {
             pedidoIdEdicion = getIntent().getLongExtra("pedido_id", 0);
             if (pedidoIdEdicion > 0) {
                 loadPedidoData(pedidoIdEdicion);
             }
        }
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

    private void loadPedidoData(long id) {
        viewModel.getPedido(id).observe(this, pedido -> {
            if (pedido != null) {
                getSupportActionBar().setTitle("Editar Pedido #" + pedido.getId());
                binding.btnGuardar.setText("Actualizar Pedido");
                
                originalMonto = pedido.getMontoCompra(); // Guardar para validar cambios
                binding.etMontoCompra.setText(String.valueOf(pedido.getMontoCompra()));
                
                binding.rbGananciaFija.setChecked(true); 
                binding.etGanancia.setText(String.valueOf(pedido.getGanancia()));
                binding.etNotas.setText(pedido.getNotas());
                
                if (pedido.fechaRegistroEpoch != null) {
                    fechaRegistro.setTimeInMillis(pedido.fechaRegistroEpoch);
                    binding.btnFechaRegistro.setText(new java.text.SimpleDateFormat("dd/MM/yyyy").format(fechaRegistro.getTime()));
                }
                 if (pedido.fechaEntregaEpoch != null) {
                    fechaEntrega.setTimeInMillis(pedido.fechaEntregaEpoch);
                    binding.btnFechaEntrega.setText(new java.text.SimpleDateFormat("dd/MM/yyyy").format(fechaEntrega.getTime()));
                }
                
                selectSpinnerItem(binding.spinnerCliente, pedido.clienteId);
                selectSpinnerItem(binding.spinnerTienda, pedido.tiendaId);
                selectSpinnerItem(binding.spinnerTarjeta, pedido.tarjetaId);
            }
        });
    }

    private void selectSpinnerItem(android.widget.Spinner spinner, Long id) {
        if (id == null) return;
        android.widget.Adapter adapter = spinner.getAdapter();
        if (adapter == null) return;
        for (int i = 0; i < adapter.getCount(); i++) {
             Object item = adapter.getItem(i);
             if (item instanceof Cliente && ((Cliente)item).id == id) {
                 spinner.setSelection(i); return;
             }
             if (item instanceof Tienda && ((Tienda)item).id == id) {
                 spinner.setSelection(i); return;
             }
             if (item instanceof Tarjeta && ((Tarjeta)item).id == id) {
                 spinner.setSelection(i); return;
             }
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
            Toast.makeText(this, "Verifica los valores numéricos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // VALIDACIÓN INTELIGENTE DE LÍMITE DE TARJETA
        double diferencia = (pedidoIdEdicion > 0) ? monto - originalMonto : monto;
        if (tarjeta.getLimiteCredito() > 0 && (tarjeta.getDeudaActual() + diferencia) > tarjeta.getLimiteCredito()) {
            Toast.makeText(this, "El monto de la compra supera el limite de la tarjeta.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isGananciaFija = binding.rbGananciaFija.isChecked();
        double totalGanancia = isGananciaFija ? ganancia : monto * (ganancia / 100);
        double totalGeneral = monto + totalGanancia;

        Pedido pedido = new Pedido();
        if (pedidoIdEdicion > 0) {
            pedido.id = pedidoIdEdicion;
        }
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
        Toast.makeText(this, "Pedido actualizado", Toast.LENGTH_SHORT).show();
        finish();
    }
}
