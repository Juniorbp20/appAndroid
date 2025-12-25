package com.example.gestiondecompras.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestiondecompras.R;
import com.example.gestiondecompras.models.ClienteGanancia;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ClienteGananciaAdapter extends RecyclerView.Adapter<ClienteGananciaAdapter.ViewHolder> {

    private List<ClienteGanancia> lista = Collections.emptyList();

    public void actualizarLista(List<ClienteGanancia> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cliente_ganancia, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClienteGanancia item = lista.get(position);
        holder.tvNombre.setText(item.clienteNombre);
        holder.tvGanancia.setText(String.format(Locale.getDefault(), "RD$ %,.2f", item.totalGanancia));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvGanancia;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvClienteNombre);
            tvGanancia = itemView.findViewById(R.id.tvClienteGanancia);
        }
    }
}
