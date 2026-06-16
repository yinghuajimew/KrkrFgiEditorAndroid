package com.krkr.fgieditor.model;

import android.graphics.Bitmap;

public class Layer {
    public String name;
    public int x;
    public int y;
    public int opacity; // 0-255
    public int layerId;
    public Bitmap bitmap;

    public Layer(String name, int x, int y, int opacity, int layerId, Bitmap bitmap) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.opacity = opacity;
        this.layerId = layerId;
        this.bitmap = bitmap;
    }

    // Empty layer constructor (for "none" placeholder)
    public Layer(String name, int layerId) {
        this.name = name;
        this.layerId = layerId;
        this.x = 0;
        this.y = 0;
        this.opacity = 255;
        this.bitmap = null;
    }

    public boolean isEmpty() {
        return bitmap == null;
    }

    @Override
    public String toString() {
        return name;
    }
}
