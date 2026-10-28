package com.example.gestiondecompras.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import com.example.gestiondecompras.R;

import java.util.ArrayList;
import java.util.List;

public class TutorialOverlayView extends FrameLayout {

    public interface Listener {
        void onPasoMostrado(int indice);
        void onTerminado();
        void onSaltado();
    }

    public static class Paso {
        public final View objetivo;
        public final String titulo;
        public final String descripcion;

        public Paso(View objetivo, String titulo, String descripcion) {
            this.objetivo = objetivo;
            this.titulo = titulo;
            this.descripcion = descripcion;
        }
    }

    private final List<Paso> pasos;
    private final Listener listener;
    private int indice = 0;
    private final float density;

    private final View topBand, bottomBand, leftBand, rightBand, relleno, fullDim, ring;
    private final androidx.cardview.widget.CardView card;
    private final TextView contador, titulo, cuerpo, saltar;
    private final Button siguiente;

    @SuppressLint("ClickableViewAccessibility")
    public TutorialOverlayView(Context context, List<Paso> pasos, Listener listener) {
        super(context);
        this.pasos = pasos != null ? pasos : new ArrayList<>();
        this.listener = listener;
        density = getResources().getDisplayMetrics().density;
int dimColor = ContextCompat.getColor(context, R.color.tutorial_dim);
        int primario = ContextCompat.getColor(context, R.color.primary_color);
        int colorTitulo = ContextCompat.getColor(context, R.color.tutorial_title);
        int colorCuerpo = ContextCompat.getColor(context, R.color.tutorial_body);
        int pad = (int) (16f * density);

        GradientDrawable ringDrawable = new GradientDrawable();
        ringDrawable.setColor(Color.TRANSPARENT);
        ringDrawable.setStroke((int) (3f * density), Color.WHITE);
        ringDrawable.setCornerRadius(24f * density);
        ring = new View(context);
        ring.setBackground(ringDrawable);
        addView(ring, new FrameLayout.LayoutParams(0, 0));

        card = new androidx.cardview.widget.CardView(context);
        card.setRadius(20f * density);
        card.setCardElevation(12f * density);
        card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.tutorial_card));
        addView(card, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) (230f * density), Gravity.BOTTOM));

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(pad, pad, pad, pad);

        contador = new TextView(context);
        contador.setTextSize(13f);
        contador.setTextColor(colorCuerpo);

        saltar = new TextView(context);
        saltar.setText("\u2715  Saltar");
        saltar.setTextSize(14f);
        saltar.setTextColor(primario);
        saltar.setTypeface(saltar.getTypeface(), Typeface.BOLD);
        saltar.setGravity(Gravity.END);
        saltar.setPadding((int) (12f * density), 0, 0, 0);
        saltar.setOnClickListener(v -> dismiss());

        LinearLayout rowTop = new LinearLayout(context);
        rowTop.setOrientation(LinearLayout.HORIZONTAL);
        rowTop.addView(contador, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        rowTop.addView(saltar, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        content.addView(rowTop);

        titulo = new TextView(context);
        titulo.setTextSize(19f);
        titulo.setTextColor(colorTitulo);
        titulo.setTypeface(titulo.getTypeface(), Typeface.BOLD);
        LinearLayout.LayoutParams lpTitle = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpTitle.topMargin = (int) (6f * density);
        content.addView(titulo, lpTitle);

        cuerpo = new TextView(context);
        cuerpo.setTextSize(15f);
        cuerpo.setTextColor(colorCuerpo);
        cuerpo.setLineSpacing(0f, 1.15f);
        LinearLayout.LayoutParams lpBody = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpBody.topMargin = (int) (6f * density);
        content.addView(cuerpo, lpBody);

        siguiente = new Button(context);
        siguiente.setText("Siguiente");
        siguiente.setTextSize(15f);
        siguiente.setTextColor(Color.WHITE);
        siguiente.setTypeface(siguiente.getTypeface(), Typeface.BOLD);
        siguiente.setAllCaps(false);
        siguiente.setBackgroundTintList(ColorStateList.valueOf(primario));
        siguiente.setOnClickListener(v -> avanzar());

        LinearLayout rowBtn = new LinearLayout(context);
        rowBtn.setOrientation(LinearLayout.HORIZONTAL);
        rowBtn.setGravity(Gravity.END);
        LinearLayout.LayoutParams lpRow = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpRow.topMargin = (int) (14f * density);
        rowBtn.addView(siguiente, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        content.addView(rowBtn, lpRow);

        card.addView(content);

        fullDim = nuevoBand(dimColor);
        addView(fullDim, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        topBand = nuevoBand(dimColor);
        bottomBand = nuevoBand(dimColor);
        leftBand = nuevoBand(dimColor);
        rightBand = nuevoBand(dimColor);
        relleno = nuevoBand(dimColor);
        addView(topBand, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0));
        addView(bottomBand, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0));
        addView(leftBand, new FrameLayout.LayoutParams(0, 0));
        addView(rightBand, new FrameLayout.LayoutParams(0, 0));
        addView(relleno, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0));
    }
    private boolean necesitaReaplicar = true;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (necesitaReaplicar) {
            necesitaReaplicar = false;
            aplicarPaso();
        }
    }

    private View nuevoBand(int color) {
        View v = new View(getContext());
        v.setBackgroundColor(color);
        return v;
    }

    private void aplicarPaso() {
        if (pasos.isEmpty()) return;
        Paso paso = pasos.get(indice);
        contador.setText("Paso " + (indice + 1) + " de " + pasos.size());
        titulo.setText(paso.titulo);
        cuerpo.setText(paso.descripcion);
        siguiente.setText(indice + 1 >= pasos.size() ? "Finalizar" : "Siguiente");

        float cardH = 260f * density;
        int m = (int) (20f * density);
        FrameLayout.LayoutParams lp;

        if (paso.objetivo == null) {
            fullDim.setVisibility(VISIBLE);
            for (View v : new View[]{topBand, bottomBand, leftBand, rightBand, relleno, ring}) {
                v.setVisibility(GONE);
            }
            lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) cardH, Gravity.CENTER);
            lp.setMargins(m, 0, m, 0);
            card.setLayoutParams(lp);
            requestLayout();
            return;
        }

        fullDim.setVisibility(GONE);
        for (View v : new View[]{topBand, bottomBand, leftBand, rightBand, relleno, ring}) {
            v.setVisibility(VISIBLE);
        }

        int[] loc = new int[2];
        paso.objetivo.getLocationOnScreen(loc);
        int[] origin = new int[2];
        getLocationOnScreen(origin);
        float gap = 12f * density;
        float borde = 3f * density;
        int x0 = loc[0] - origin[0] - (int) gap;
        int y0 = loc[1] - origin[1] - (int) gap;
        int anchoHueco = paso.objetivo.getWidth() + (int) (2f * gap);
        int altoHueco = paso.objetivo.getHeight() + (int) (2f * gap);
        int x1 = x0 + anchoHueco;
        int y1 = y0 + altoHueco;
        int W = getWidth();
        int H = getHeight();

        boolean cardArriba = (y0 + altoHueco / 2f) > H * 0.55f;
        int cardTop;
        int cardBot;
        int rellenoTop;
        int rellenoH; 
        if (cardArriba) {
            cardBot = Math.min(m + (int) cardH, y0);
            cardTop = cardBot - (int) cardH;
            rellenoTop = cardBot;
            rellenoH = Math.max(0, y0 - cardBot);
        } else {
            cardTop = Math.max(H - m - (int) cardH, y1);
            cardBot = cardTop + (int) cardH;
            rellenoTop = y1;
            rellenoH = Math.max(0, cardTop - y1);
        }

        lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, cardArriba ? Math.max(0, cardTop) : y0);
        topBand.setLayoutParams(lp);
        lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, Math.max(0, H - cardBot));
        lp.topMargin = cardBot;
        bottomBand.setLayoutParams(lp);
        lp = new FrameLayout.LayoutParams(x0, altoHueco);
        lp.topMargin = y0;
        leftBand.setLayoutParams(lp);
        lp = new FrameLayout.LayoutParams(W - x1, altoHueco);
        lp.leftMargin = x1;
        lp.topMargin = y0;
        rightBand.setLayoutParams(lp);
        lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, rellenoH);
        lp.topMargin = rellenoTop;
        relleno.setLayoutParams(lp);
        lp = new FrameLayout.LayoutParams((int) (anchoHueco + 2f * borde), (int) (altoHueco + 2f * borde));
        lp.leftMargin = x0 - (int) borde;
        lp.topMargin = y0 - (int) borde;
        ring.setLayoutParams(lp);

        lp = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int) cardH, cardArriba ? Gravity.TOP : Gravity.BOTTOM);
        lp.setMargins(m, cardArriba ? cardTop : 0, m, cardArriba ? 0 : H - cardBot);
        card.setLayoutParams(lp);
        requestLayout();
    }

    private void avanzar() {
        if (indice + 1 >= pasos.size()) {
            if (listener != null) listener.onTerminado();
            return;
        }
        indice++;
        aplicarPaso();
        if (listener != null) listener.onPasoMostrado(indice);
    }

    private void dismiss() {
        if (listener != null) listener.onSaltado();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}