package com.example.gestiondecompras.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestiondecompras.R;
import com.example.gestiondecompras.adapters.PedidosAdapter;
import com.example.gestiondecompras.databinding.ActivityListaPedidosBinding;
import com.example.gestiondecompras.models.Pedido;
import com.example.gestiondecompras.viewmodels.ListaPedidosViewModel;
import com.google.android.material.snackbar.Snackbar;

public class ListaPedidosActivity extends AppCompatActivity implements PedidosAdapter.OnPedidoClickListener {

    private ActivityListaPedidosBinding binding;
    private ListaPedidosViewModel viewModel;
    private PedidosAdapter adapter;
    private Integer filtroClienteId = null;
    private String filtroClienteNombre = null;
    private final String[] estados = {"Todos", "Por Cobrar", "Pendientes", "Entregados", "Pagados", "Atrasados"};
    private boolean filtroAtrasados = false;

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
            if (getIntent().hasExtra("filtro_atrasados")) {
                filtroAtrasados = getIntent().getBooleanExtra("filtro_atrasados", false);
            }
        }

        setupRecyclerView();
        setupFilters();
        setupFab();
        observeViewModel();

        actualizarTitulo();
        // cargarPedidos(); // Moved to after filter setup to avoid double load or wrong filter
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

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                Pedido pedido = adapter.getPedidoAt(position);
                if (direction == ItemTouchHelper.LEFT && pedido != null) {
                    confirmarEliminarPedido(pedido, position);
                }
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvPedidos);
    }

    private void setupFilters() {
        binding.spinnerFiltroEstado.setAdapter(
                new ArrayAdapter<>(this, R.layout.item_dropdown, estados)
        );

        int selection = 0; // Default: Todos
        if (filtroAtrasados) {
            selection = 5; // Atrasados
        } else if (getIntent() != null && getIntent().hasExtra("filtro_estado")) {
            String extra = getIntent().getStringExtra("filtro_estado");
            if (extra != null) {
                switch (extra.toLowerCase()) {
                    case "por_cobrar": selection = 1; break;
                    case "pendiente": selection = 2; break;
                    case "entregado": selection = 3; break;
                    case "pagado": selection = 4; break;
                    // default 0
                }
            }
        } else {
            selection = 0;
        }

        binding.spinnerFiltroEstado.setText(estados[selection], false);

        binding.spinnerFiltroEstado.setOnItemClickListener((parent, view, position, id) -> cargarPedidos());

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
                binding.emptyState.setVisibility(pedidos.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void cargarPedidos() {
        String filtroEstado = binding.spinnerFiltroEstado.getText() != null
                ? binding.spinnerFiltroEstado.getText().toString() : "";
        String busqueda = binding.etBuscar.getText().toString();
        viewModel.loadPedidos(filtroEstado, busqueda, filtroClienteId);
    }

    @Override
    public void onPedidoClick(Pedido pedido) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_detalle_pedido, null);
        
        android.widget.TextView tvProducto = view.findViewById(R.id.tv_detalle_producto);
        android.widget.TextView tvTienda = view.findViewById(R.id.tv_detalle_tienda);
        android.widget.TextView tvFechaCompra = view.findViewById(R.id.tv_detalle_fecha_compra);
        android.widget.TextView tvFechaEntrega = view.findViewById(R.id.tv_detalle_fecha_entrega);
        android.widget.TextView tvCosto = view.findViewById(R.id.tv_detalle_costo);
        android.widget.TextView tvVenta = view.findViewById(R.id.tv_detalle_venta);
        android.widget.TextView tvGanancia = view.findViewById(R.id.tv_detalle_ganancia);
        android.widget.TextView tvEstado = view.findViewById(R.id.tv_detalle_estado);
        com.google.android.material.button.MaterialButton btnEnviar = view.findViewById(R.id.btn_enviar_resumen);
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        
        tvProducto.setText(pedido.getClienteNombre());
        tvTienda.setText(pedido.getTienda());
        
        tvFechaCompra.setText(pedido.getFechaRegistro() != null ? sdf.format(pedido.getFechaRegistro()) : "N/A");
        tvFechaEntrega.setText(pedido.getFechaEntrega() != null ? sdf.format(pedido.getFechaEntrega()) : "N/A");
        
        tvCosto.setText(String.format("RD$ %,.2f", pedido.getMontoCompra()));
        tvVenta.setText(String.format("RD$ %,.2f", pedido.getTotalGeneral()));
        tvGanancia.setText(String.format("RD$ %,.2f", pedido.getGanancia()));
        
        tvEstado.setText(pedido.getEstado());
        
        int colorRes;
        if (Pedido.ESTADO_PAGADO.equalsIgnoreCase(pedido.getEstado())) {
            colorRes = R.color.status_paid;
        } else if (Pedido.ESTADO_ENTREGADO.equalsIgnoreCase(pedido.getEstado())) {
            colorRes = R.color.status_delivered;
        } else if (Pedido.ESTADO_CANCELADO.equalsIgnoreCase(pedido.getEstado())) {
            colorRes = R.color.status_cancelled;
        } else {
            colorRes = R.color.status_pending;
        }
        
        tvEstado.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
            androidx.core.content.ContextCompat.getColor(this, colorRes)
        ));
        tvEstado.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.white));

        btnEnviar.setOnClickListener(v -> enviarResumenCliente(pedido, sdf));

        builder.setView(view)
               .setPositiveButton("Cerrar", null)
               .setNeutralButton("Editar", (dialog, which) -> {
                   Intent i = new Intent(this, NuevoPedidoActivity.class);
                   i.putExtra("pedido_id", pedido.getId());
                   startActivity(i);
               })
               .show();
    }

    private void enviarResumenCliente(Pedido pedido, java.text.SimpleDateFormat sdf) {
        String fechaRegistro = pedido.getFechaRegistro() != null ? sdf.format(pedido.getFechaRegistro()) : "N/A";
        String fechaEntrega = pedido.getFechaEntrega() != null ? sdf.format(pedido.getFechaEntrega()) : "N/A";

        String mensaje = "*Resumen del Pedido*\n\n"
                + getString(R.string.pedido_detalle_cliente) + ": " + pedido.getClienteNombre() + "\n"
                + getString(R.string.pedido_detalle_tienda) + ": " + pedido.getTienda() + "\n"
                + getString(R.string.pedido_detalle_fecha_compra) + ": " + fechaRegistro + "\n"
                + getString(R.string.pedido_detalle_fecha_entrega) + ": " + fechaEntrega + "\n"
                + getString(R.string.pedido_detalle_costo) + ": RD$ " + String.format(java.util.Locale.getDefault(), "%,.2f", pedido.getMontoCompra()) + "\n"
                + getString(R.string.pedido_detalle_venta) + ": RD$ " + String.format(java.util.Locale.getDefault(), "%,.2f", pedido.getTotalGeneral()) + "\n"
                + getString(R.string.pedido_detalle_ganancia) + ": RD$ " + String.format(java.util.Locale.getDefault(), "%,.2f", pedido.getGanancia()) + "\n"
                + "Estado: " + pedido.getEstado();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.pedido_share_subject, pedido.getClienteNombre()));
        intent.putExtra(Intent.EXTRA_TEXT, mensaje);
        startActivity(Intent.createChooser(intent, getString(R.string.pedido_enviar_resumen)));
    }

    @Override
    public void onPedidoLongClick(Pedido pedido) {
        if (pedido != null) {
            mostrarDialogoEstado(pedido);
        }
    }

    private void actualizarTitulo() {
        if (filtroClienteNombre != null) {
            binding.toolbar.setTitle(getString(R.string.pedidos_title_cliente, filtroClienteNombre));
        } else {
            binding.toolbar.setTitle(R.string.pedidos_title);
        }
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

    private void mostrarDialogoEstado(Pedido pedido) {
        final String[] valores = {
                Pedido.ESTADO_PENDIENTE,
                Pedido.ESTADO_ENTREGADO,
                Pedido.ESTADO_PAGADO,
                Pedido.ESTADO_CANCELADO
        };
        final String[] labels = getResources().getStringArray(R.array.pedido_estados_labels);
        int seleccionado = 0;
        for (int i = 0; i < valores.length; i++) {
            if (valores[i].equalsIgnoreCase(pedido.estado)) {
                seleccionado = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_change_estado_title)
                .setSingleChoiceItems(labels, seleccionado, (dialog, which) -> {
                    pedido.estado = valores[which];
                    viewModel.updatePedido(pedido);
                    Snackbar.make(binding.rvPedidos, getString(R.string.pedidos_estado_actualizado, labels[which]), Snackbar.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.accion_cancelar, null)
                .show();
    }
}

