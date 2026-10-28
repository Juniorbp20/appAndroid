package com.example.gestiondecompras.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gestiondecompras.R;
import com.example.gestiondecompras.models.Abono;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AbonosAdapter extends RecyclerView.Adapter<AbonosAdapter.ViewHolder> {

    public interface OnAbonoClickListener {
        void onAbonoClick(Abono abono);
    }

    private final List<Abono> abonos = new ArrayList<>();
    private final OnAbonoClickListener listener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public AbonosAdapter(OnAbonoClickListener listener) {
        this.listener = listener;
    }

    public void actualizarLista(List<Abono> lista) {
        abonos.clear();
        if (lista != null) {
            abonos.addAll(lista);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_abono, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Abono abono = abonos.get(position);
        holder.tvMonto.setText(String.format(Locale.getDefault(), "RD$ %,.2f", abono.getMonto()));
        holder.tvFecha.setText(sdf.format(new Date(abono.getFechaEpoch())));
        holder.tvMetodo.setText(abono.getMetodo());

        boolean tieneNotas = abono.getNotas() != null && !abono.getNotas().trim().isEmpty();
        holder.tvNotas.setVisibility(tieneNotas ? View.VISIBLE : View.GONE);
        holder.tvNotas.setText(tieneNotas ? abono.getNotas() : "");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAbonoClick(abono);
            }
        });
    }

    @Override
    public int getItemCount() {
        return abonos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvMonto;
        final TextView tvFecha;
        final TextView tvMetodo;
        final TextView tvNotas;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMonto = itemView.findViewById(R.id.tv_monto_abono);
            tvFecha = itemView.findViewById(R.id.tv_fecha_abono);
            tvMetodo = itemView.findViewById(R.id.tv_metodo_abono);
            tvNotas = itemView.findViewById(R.id.tv_notas_abono);
        }
    }
}
