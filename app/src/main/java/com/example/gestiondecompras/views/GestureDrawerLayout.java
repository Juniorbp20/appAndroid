package com.example.gestiondecompras.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.drawerlayout.widget.DrawerLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class GestureDrawerLayout extends DrawerLayout {

    private static final float EDGE_FRACTION = 0.33f;
    private int lastWidth = 0;

    public GestureDrawerLayout(Context context) {
        super(context);
    }

    public GestureDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GestureDrawerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        if (width > 0 && width != lastWidth) {
            lastWidth = width;
            setLeftEdgeSize(Math.round(width * EDGE_FRACTION));
        }
    }

    private void setLeftEdgeSize(int edgeSize) {
        try {
            Field field = DrawerLayout.class.getDeclaredField("mLeftDragger");
            field.setAccessible(true);
            Object dragger = field.get(this);
            if (dragger == null) return;
            Method setEdgeSize = dragger.getClass().getMethod("setEdgeSize", int.class);
            setEdgeSize.invoke(dragger, edgeSize);
        } catch (Exception ignored) {
        }
    }
}