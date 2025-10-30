package com.example.gestiondecompras.activities;

import android.content.Intent;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestiondecompras.R;
import com.example.gestiondecompras.adapters.PedidosAdapter;
import com.example.gestiondecompras.database.DatabaseHelper;
import com.example.gestiondecompras.databinding.ActivityListaPedidosBinding;
import com.example.gestiondecompras.models.Cliente;
import com.example.gestiondecompras.models.Pedido;
import com.example.gestiondecompras.viewmodels.ListaPedidosViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ListaPedidosActivity extends AppCompatActivity implements PedidosAdapter.OnPedidoClickListener {

    private ActivityListaPedidosBinding binding;
    private ListaPedidosViewModel viewModel;
    private PedidosAdapter adapter;
    private Integer filtroClienteId = null;
    private String filtroClienteNombre = null;
    private final String[] estados = {"Todos", "Pendientes", "Entregados", "Pagados", "Atrasados"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListaPedidosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ListaPedidosViewModel.class);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        if (getIntent() != null) {
            if (getIntent().hasExtra("cliente_id")) {
                filtroClienteId = getIntent().getIntExtra("cliente_id", -1);
                if (filtroClienteId <= 0) filtroClienteId = null;
            }
            if (getIntent().hasExtra("cliente_nombre")) {
                filtroClienteNombre = getIntent().getStringExtra("cliente_nombre");
            }
        }

        setupRecyclerView();
        setupFilters();
        setupFab();
        observeViewModel();

        actualizarTitulo();
        cargarPedidos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarPedidos();
    }

    private void setupRecyclerView() {
        binding.rvPedidos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PedidosAdapter(this);
        binding.rvPedidos.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Pedido pedido = adapter.getPedidoAt(position);

                if (direction == ItemTouchHelper.RIGHT) {
                    String nuevoEstado = obtenerSiguienteEstado(pedido.estado);
                    pedido.estado = nuevoEstado;
                    viewModel.updatePedido(pedido);
                    Snackbar.make(binding.rvPedidos, getString(R.string.pedidos_estado_actualizado, nuevoEstado), Snackbar.LENGTH_SHORT).show();
                } else if (direction == ItemTouchHelper.LEFT) {
                    confirmarEliminarPedido(pedido, position);
                }
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvPedidos);
    }

    private void setupFilters() {
        binding.spinnerFiltroEstado.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, estados)
        );
        binding.spinnerFiltroEstado.setSelection(1); // Pendientes por defecto

        binding.spinnerFiltroEstado.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                cargarPedidos();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        binding.etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { cargarPedidos(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFab() {
        binding.fabNuevoPedido.setOnClickListener(v -> {
            startActivity(new Intent(this, NuevoPedidoActivity.class));
        });
    }

    private void observeViewModel() {
        viewModel.getPedidos().observe(this, pedidos -> {
            if (pedidos != null) {
                adapter.actualizarLista(pedidos);
                binding.tvVacio.setVisibility(pedidos.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void cargarPedidos() {
        String filtroEstado = (String) binding.spinnerFiltroEstado.getSelectedItem();
        String busqueda = binding.etBuscar.getText().toString();
        viewModel.loadPedidos(filtroEstado, busqueda, filtroClienteId);
    }

    @Override
    public void onPedidoClick(Pedido pedido) {
        Intent i = new Intent(this, NuevoPedidoActivity.class);
        i.putExtra("pedido_id", pedido.id);
        startActivity(i);
    }

    @Override
    public void onPedidoLongClick(Pedido pedido) {
        onPedidoClick(pedido);
    }

    private void confirmarEliminarPedido(Pedido pedido, int position) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_pedido_title)
                .setMessage(R.string.dialog_delete_pedido_message)
                .setPositiveButton(R.string.accion_eliminar, (dialog, which) -> {
                    viewModel.deletePedido(pedido);
                    Snackbar.make(binding.rvPedidos, R.string.snackbar_pedido_eliminado, Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.accion_cancelar, (dialog, which) -> adapter.notifyItemChanged(position))
                .setOnCancelListener(dialog -> adapter.notifyItemChanged(position))
                .show();
    }

    private void actualizarTitulo() {
        if (filtroClienteNombre != null) {
            binding.toolbar.setTitle(getString(R.string.pedidos_title_cliente, filtroClienteNombre));
        } else {
            binding.toolbar.setTitle(R.string.pedidos_title);
        }
    }

    private String obtenerSiguienteEstado(String estadoActual) {
        if (Pedido.ESTADO_PENDIENTE.equals(estadoActual)) return Pedido.ESTADO_ENTREGADO;
        if (Pedido.ESTADO_ENTREGADO.equals(estadoActual)) return Pedido.ESTADO_PAGADO;
        if (Pedido.ESTADO_PAGADO.equals(estadoActual)) return Pedido.ESTADO_PENDIENTE;
        if (Pedido.ESTADO_CANCELADO.equals(estadoActual)) return Pedido.ESTADO_PENDIENTE;
        return Pedido.ESTADO_PENDIENTE;
    }
}
