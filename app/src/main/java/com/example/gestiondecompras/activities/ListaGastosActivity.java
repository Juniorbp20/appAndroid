package com.example.gestiondecompras.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.gestiondecompras.R;
import com.example.gestiondecompras.adapters.GastosAdapter;
import com.example.gestiondecompras.databinding.ActivityListaGastosBinding;
import com.example.gestiondecompras.models.Gasto;
import com.example.gestiondecompras.viewmodels.ListaGastosViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ListaGastosActivity extends AppCompatActivity implements GastosAdapter.OnGastoClickListener {

    public static final String EXTRA_GASTO_ID = "gasto_id";

    private ActivityListaGastosBinding binding;
    private ListaGastosViewModel viewModel;
    private GastosAdapter adapter;

    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private Long fechaDesde = null;
    private Long fechaHasta = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListaGastosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ListaGastosViewModel.class);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        setupRecyclerView();
        setupFiltros();
        observeViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarGastos();
    }

    private void setupRecyclerView() {
        binding.rvGastos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GastosAdapter(this);
        binding.rvGastos.setAdapter(adapter);
    }

    private void setupFiltros() {
        binding.fabNuevoGasto.setOnClickListener(v ->
                startActivity(new Intent(this, NuevoGastoActivity.class)));

        binding.etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { cargarGastos(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        binding.etFechaDesde.setOnClickListener(v -> mostrarDatePicker(true));
        binding.etFechaHasta.setOnClickListener(v -> mostrarDatePicker(false));
        binding.etFechaDesde.setOnLongClickListener(v -> {
            fechaDesde = null;
            binding.etFechaDesde.setText("");
            cargarGastos();
            return true;
        });
        binding.etFechaHasta.setOnLongClickListener(v -> {
            fechaHasta = null;
            binding.etFechaHasta.setText("");
            cargarGastos();
            return true;
        });
    }

    private void mostrarDatePicker(boolean esDesde) {
        Calendar cal = Calendar.getInstance();
        if (esDesde && fechaDesde != null) {
            cal.setTimeInMillis(fechaDesde);
        } else if (!esDesde && fechaHasta != null) {
            cal.setTimeInMillis(fechaHasta);
        }

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar seleccionada = Calendar.getInstance();
            seleccionada.set(year, month, dayOfMonth, 0, 0, 0);
            seleccionada.set(Calendar.MILLISECOND, 0);
            if (esDesde) {
                fechaDesde = seleccionada.getTimeInMillis();
                binding.etFechaDesde.setText(sdf.format(new Date(fechaDesde)));
            } else {
                seleccionada.set(Calendar.HOUR_OF_DAY, 23);
                seleccionada.set(Calendar.MINUTE, 59);
                seleccionada.set(Calendar.SECOND, 59);
                fechaHasta = seleccionada.getTimeInMillis();
                binding.etFechaHasta.setText(sdf.format(new Date(fechaHasta)));
            }
            cargarGastos();
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void cargarGastos() {
        String busqueda = binding.etBuscar.getText() != null
                ? binding.etBuscar.getText().toString().trim() : "";
        viewModel.loadGastos(fechaDesde, fechaHasta, busqueda);
    }

    private void observeViewModel() {
        viewModel.getGastos().observe(this, gastos -> {
            if (gastos != null) {
                adapter.actualizarLista(gastos);
                binding.emptyState.setVisibility(gastos.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getTotalMes().observe(this, total -> {
            if (total != null) {
                binding.tvTotalMes.setText(String.format(Locale.getDefault(), "RD$ %,.2f", total));
            }
        });

        viewModel.getTotalesPorCategoria().observe(this, categorias -> {
            if (categorias == null || categorias.isEmpty()) {
                binding.tvCantidadMes.setText(getString(R.string.gastos_cantidad_placeholder));
                return;
            }
            int cantidad = 0;
            for (com.example.gestiondecompras.models.GastoCategoria c : categorias) {
                cantidad += c.getCantidad();
            }
            binding.tvCantidadMes.setText(getString(R.string.gastos_cantidad_format, cantidad));
        });
    }

    @Override
    public void onGastoClick(Gasto gasto) {
        mostrarOpcionesGasto(gasto);
    }

    @Override
    public void onGastoLongClick(Gasto gasto) {
        mostrarOpcionesGasto(gasto);
    }

    private void mostrarOpcionesGasto(Gasto gasto) {
        CharSequence[] opciones = {
                getString(R.string.accion_editar),
                getString(R.string.accion_eliminar)
        };

        new AlertDialog.Builder(this)
                .setTitle(gasto.getDescripcion())
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        editarGasto(gasto);
                    } else if (which == 1) {
                        confirmarEliminar(gasto);
                    }
                })
                .setNegativeButton(R.string.accion_cancelar, null)
                .show();
    }

    private void editarGasto(Gasto gasto) {
        Intent intent = new Intent(this, NuevoGastoActivity.class);
        intent.putExtra(EXTRA_GASTO_ID, gasto.getId());
        startActivity(intent);
    }

    private void confirmarEliminar(Gasto gasto) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.gastos_eliminar_title)
                .setMessage(R.string.gastos_eliminar_message)
                .setPositiveButton(R.string.accion_eliminar, (d, w) -> {
                    viewModel.deleteGasto(gasto);
                    Toast.makeText(this, R.string.toast_gasto_eliminado, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.accion_cancelar, null)
                .show();
    }
}
